package service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.StandardCopyOption;
import java.security.cert.Extension;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

@CommonsLog
public class CloudStorageClient {

    static final String TEMP_DIR_NAME = "./resources/";
    static final String TEMP_FILE_NAME = "temp";

    private final Properties appProps;
    private final String verificationBucketName;
    private final String verificationFolderName;
    private final String fileExtension;

    private final AmazonS3 s3client;

    public CloudStorageClient(Properties appProps) {
        this.appProps = appProps;
        this.verificationBucketName = this.appProps.getProperty("aws.s3.loaded.bucket.name");
        this.verificationFolderName = this.appProps.getProperty("aws.s3.loaded.folder.name");
        this.fileExtension = this.appProps.getProperty("candle-validation.fileExtension");
        this.s3client = buildAmazonClient(this.appProps);
    }

    private static AmazonS3 buildAmazonClient(Properties appProps) {
        return AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(appProps.getProperty("aws.access.key"), appProps.getProperty("aws.secret.key"))))
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(appProps.getProperty("aws.access.endpoint"), Regions.US_EAST_1.name()))
                .build();
    }

    private boolean checkBucketAndObject(String bucket, String key) {
        return (s3client.doesBucketExistV2(bucket) && s3client.doesObjectExist(bucket, key));
    }

    public boolean isFileExisted(String key) {
        String path;
        if (verificationFolderName != null) {
            path = verificationFolderName  + key;
        } else {
            path = key;
        }
        boolean result = checkBucketAndObject(verificationBucketName, path);
        log.info(key + " exists: " + result);
        return result;
    }

    public List<String> getVerificationObjects() {
        List<String> list;
        String removeKey;
        if (verificationFolderName != null) {
            list = filesListByBucketAndPrefix(verificationBucketName, verificationFolderName);
            removeKey = verificationFolderName + "/" + appProps.getProperty("aws.s3.loaded.flag");
        } else {
            list = filesListByBucket(verificationBucketName);
            removeKey = appProps.getProperty("aws.s3.loaded.flag");
        }
        list.remove(removeKey);
        return list;
    }

    private List<String> filesListByBucketAndPrefix(String bucket, String prefix) {
        return s3client.listObjectsV2(bucket, prefix)
                .getObjectSummaries()
                .stream()
                .map(S3ObjectSummary::getKey)
                .collect(Collectors.toList());
    }

    private List<String> filesListByBucket(String bucket) {
        return s3client.listObjectsV2(bucket)
                .getObjectSummaries()
                .stream()
                .map(S3ObjectSummary::getKey)
                .collect(Collectors.toList());
    }

    public File downloadFile(String fileName) throws Exception {
        StringBuilder path = new StringBuilder();
        if (verificationFolderName != null) {
            path.append(verificationFolderName);
        }
        if (fileName != null && !fileName.isBlank()) {
            path.append(fileName);
        } else {
            throw new Exception("No file to download");
        }

        if (fileExtension != null) {
            path.append(fileExtension);
        }
        log.info("path = " + path);
        final S3Object s3Object = s3client.getObject(verificationBucketName, path.toString());

        InputStream inputStream = s3Object.getObjectContent();

        File tempDir = new File(TEMP_DIR_NAME);
        File tempFile = new File(TEMP_DIR_NAME + TEMP_FILE_NAME);
        try {
            if (!tempDir.exists()) {
                log.info("tempDir created: " + tempDir.mkdirs());
            }
            if (!tempFile.exists()) {
                log.info("tempFile created: " + tempFile.createNewFile());
            }
              FileUtils.copyInputStreamToFile(s3Object.getObjectContent(), tempFile);

//            java.nio.file.Files.copy(
//                    inputStream,
//                    tempFile.toPath(),
//                    StandardCopyOption.REPLACE_EXISTING);
//
//            IOUtils.closeQuietly(inputStream);
        } catch (IOException e) {
            //  log.error(e.getMessage());
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
        return tempFile;
    }

//    public void uploadReportLogToAws(String path) {
//        String key = Path.of(path).getFileName().toString();
//        if (reportBucketName == null) {
//            log.info(key + " is NOT uploaded to AWS");
//            return;
//        }
//        if (!s3client.doesBucketExistV2(reportBucketName)) {
//            s3client.createBucket(reportBucketName);
//        }
//        if (reportFolderName != null && !reportFolderName.isBlank()) {
//            key = reportFolderName + "/" + key;
//        }
//        File file = new File(path);
//        s3client.putObject(reportBucketName, key, file);
//        log.info(key + " is uploaded to AWS");
//    }


}