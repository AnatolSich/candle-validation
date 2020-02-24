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

import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

@CommonsLog
public class CloudStorageClient {

    private final Properties appProps;
    private final String verificationBucketName;
    private final String verificationFolderName;

    private final AmazonS3 s3client;

    public CloudStorageClient(Properties appProps) {
        this.appProps = appProps;
        this.verificationBucketName = this.appProps.getProperty("aws.s3.loaded.bucket.name");
        verificationFolderName = this.appProps.getProperty("aws.s3.loaded.folder.name");
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
            path = verificationFolderName + "/" + key;
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

    public InputStream downloadFile(String fileName) {
        String path;
        if (verificationFolderName != null) {
            path = verificationFolderName + "/" + fileName;
        } else {
            path = fileName;
        }
        final S3Object s3Object = s3client.getObject(verificationBucketName, path);
        return s3Object.getObjectContent();
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