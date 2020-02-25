
import exceptions.ValidationException;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.log4j.PropertyConfigurator;
import service.CloudStorageClient;
import service.ParseCsvService;
import service.TimeService;
import service.WebhookClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;


@SuppressWarnings("WeakerAccess")

@CommonsLog
public class Main {

    private static final String PROPS_PATH = "/candle-validation/configs/";

    public static void main(String[] args) {

        try {
            Properties appProps = loadProperties();
            WebhookClient webhookClient = new WebhookClient(appProps);

            TimeService timeService = new TimeService(appProps);
            String fileName = timeService.getLocalDateTimeInMillis();
            log.info(fileName);

            String fileExtension = getFileExtension(appProps);
            log.info(fileExtension);

            CloudStorageClient cloudStorageClient = new CloudStorageClient(appProps, fileExtension);

            ParseCsvService parseCsvService = new ParseCsvService(appProps, fileName, fileExtension);

            boolean isFileExisted = false;
            try {
                isFileExisted = cloudStorageClient.isFileExisted(fileName);
            } catch (ValidationException ex) {
                webhookClient.sendMessageToSlack(ex.getMessage());
            }

            if (isFileExisted) {
                File file = cloudStorageClient.downloadFile(fileName);

                log.info(file.exists());
                log.info(file.getAbsolutePath());

                List<String[]> allData = parseCsvService.getRecords(file);

                try {
                    cloudStorageClient.isFileExisted(fileName);
                    log.info("checkSize = " + parseCsvService.checkSize(allData));
                    log.info("checkDescending" + parseCsvService.checkDescending(allData));
                    throw new ValidationException("File checked");
                } catch (ValidationException ex) {
                    webhookClient.sendMessageToSlack(ex.getMessage());
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
    }


    private static Properties loadProperties() throws IOException {
        File fileApp = new File(PROPS_PATH + "application.properties");
        InputStream appPath;
        if (fileApp.exists()) {
            appPath = new FileInputStream(fileApp.getPath());
        } else {
            appPath = Main.class.getResourceAsStream("/application.properties");
        }
        Properties appProps = new Properties();
        appProps.load(appPath);

        File fileLog = new File(PROPS_PATH + "log4j.properties");
        InputStream logPath;
        if (fileLog.exists()) {
            logPath = new FileInputStream(fileLog.getPath());
        } else {
            logPath = Main.class.getResourceAsStream("/log4j.properties");
        }
        Properties logProps = new Properties();
        logProps.load(logPath);
        PropertyConfigurator.configure(logProps);
        log.info("(Additional info) AppProps ready");
        return appProps;
    }

    private static String getFileExtension(Properties appProps) {
        String prop = appProps.getProperty("candle-validation.fileExtension");
        if (prop == null || prop.isBlank()) {
            return ".csv";
        } else return prop.trim();
    }

}
