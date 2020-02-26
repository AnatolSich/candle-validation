
import exceptions.ValidationException;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.text.StringSubstitutor;
import org.apache.log4j.PropertyConfigurator;
import service.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;


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

            String fileExtension = getFileExtension(appProps);
            log.info("FileExtension = " + fileExtension);

            String token = getToken(appProps);
            log.info("Token = " + token);

            CloudStorageClient cloudStorageClient = new CloudStorageClient(appProps, fileExtension, token);

            ParseCsvService parseCsvService = new ParseCsvService(appProps, fileName, fileExtension, token);

            boolean isFileExisted;
            try {
                isFileExisted = cloudStorageClient.isFileExisted(fileName);
                if (isFileExisted) {
                    File file = cloudStorageClient.downloadFile(fileName);
                    log.info("File is downloaded = " + file.exists());
                    List<String[]> allData = parseCsvService.getRecords(file);
                    log.info("All records are downloaded = " + (allData != null ? allData.size() : null));
                    log.info("Size is equal to required = " + parseCsvService.checkSize(allData));
                    log.info("Records are in descending order + " + parseCsvService.checkDescending(allData));
                    throw new ValidationException("File checked");
                }
            } catch (ValidationException ex) {
                webhookClient.sendMessageToSlack(ex.getMessage());
            }
            LogService logService = new LogService();
            //upload report to AWS
            cloudStorageClient.uploadReportLogToAws(logService.getLogFileName());
            cloudStorageClient.deleteTempFile();
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
        improveProperties(appProps);

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
        log.info("AppProps ready");
        return appProps;
    }

    private static void improveProperties(Properties appProps) {
        Set<Map.Entry<Object, Object>> set = appProps.entrySet();
        StringSubstitutor sub = new StringSubstitutor((Map) appProps);
        for (Map.Entry<Object, Object> entry : set
        ) {
            appProps.replace(entry.getKey(), sub.replace(entry.getValue()));
        }
    }

    private static String getFileExtension(Properties appProps) {
        String prop = appProps.getProperty("candle-validation.fileExtension");
        if (prop == null || prop.isBlank()) {
            return ".csv";
        } else return prop.trim();
    }

    private static String getToken(Properties appProps) {
        String prop = appProps.getProperty("aws.s3.loaded.folder.name");
        String[] splitProp = prop.split("/");
        List<String> folders = new ArrayList<>();
        for (int i = 0; i < splitProp.length; i++) {
            String str = splitProp[i].trim();
            if (!str.isBlank()) {
                folders.add(str);
            }
        }
        return folders.get(folders.size() - 1);
    }

}
