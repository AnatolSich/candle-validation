
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

    private static final String PROPS_PATH = "/s3_backup_verification/configs/";

    public static void main(String[] args) {

        try {
            Properties appProps = new Main().loadProperties();
            WebhookClient webhookClient = new WebhookClient(appProps);

            TimeService timeService = new TimeService(appProps);
            String fileName = timeService.getLocalDateTimeInMillis();
            log.info(fileName);

            CloudStorageClient cloudStorageClient = new CloudStorageClient(appProps);

            ParseCsvService parseCsvService = new ParseCsvService(appProps, fileName);


            File file = cloudStorageClient.downloadFile(fileName);

            log.info(file.exists());
            log.info(file.getAbsolutePath());

            List<String[]> allData = parseCsvService.getRecords(file);

            try {
                log.info("checkSize = " + parseCsvService.checkSize(allData));
                log.info("checkDescending" + parseCsvService.checkDescending(allData));
                throw new ValidationException("File checked");
            } catch (ValidationException ex) {
                webhookClient.sendMessageToSlack(ex.getMessage());
            }

        } catch (Exception e) {
            log.error(e.getMessage());
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
    }


    private Properties loadProperties() throws IOException {
        File fileApp = new File(PROPS_PATH + "application.properties");
        InputStream appPath;
        if (fileApp.exists()) {
            appPath = new FileInputStream(fileApp.getPath());
        } else {
            appPath = this.getClass().getResourceAsStream("/application.properties");
        }
        Properties appProps = new Properties();
        appProps.load(appPath);

        File fileLog = new File(PROPS_PATH + "log4j.properties");
        InputStream logPath;
        if (fileLog.exists()) {
            logPath = new FileInputStream(fileLog.getPath());
        } else {
            logPath = this.getClass().getResourceAsStream("/log4j.properties");
        }
        Properties logProps = new Properties();
        logProps.load(logPath);
        PropertyConfigurator.configure(logProps);
        log.info("(Additional info) AppProps ready");
        return appProps;
    }

}
