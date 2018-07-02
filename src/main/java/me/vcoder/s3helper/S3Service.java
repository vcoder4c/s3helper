package me.vcoder.s3helper;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author baodn
 * Created on 02 Apr 2018
 */
public class S3Service extends GeneralS3Service {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(S3Service.class);
    private String awsAccessKeyId;
    private String awsSecretAccessKey;
    private int maxSize; // byte
    private String bucket;
    private int timeout; //second
    private AmazonS3Client amazonS3;

    public S3Service() {
        super();
    }

    /**
     * Initilize
     * @param awsAccessKeyId
     * @param awsSecretAccessKey
     * @param maxSize
     * @param timeout
     * @param bucket
     */
    public void initialize(String awsAccessKeyId, String awsSecretAccessKey, Integer maxSize, Integer timeout, String bucket) {
        this.awsAccessKeyId = awsAccessKeyId;
        this.awsSecretAccessKey = awsSecretAccessKey;
        this.bucket = bucket;
        if(maxSize == null || maxSize == 0) {
            this.maxSize = 3145728; // 3Mb by default
        }
        if(timeout == null || timeout == 0) {
            this.timeout = 30*60; // 30 minutes by default
        }
        LOGGER.info("Initialize S3Service with maxSize:" + maxSize + " byte and accessKey:" + this.awsAccessKeyId + " and bucket:" + this.bucket + " , timeout = " + this.timeout + " seconds");

        BasicAWSCredentials awsCreds = new BasicAWSCredentials(awsAccessKeyId,
                awsSecretAccessKey);

        this.amazonS3 = (AmazonS3Client) AmazonS3ClientBuilder.standard()
                .withRegion(Regions.AP_SOUTHEAST_1)
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .build();

        LOGGER.info("Initialize S3Service successfully");
    }

    public String getPrivateAccessURL(String fileName, String directory) {
        return getAccessURL(bucket, fileName, directory, amazonS3, timeout);
    }

    public String getPublicAccessURL(String fileName, String directory) {
        return "https://" + bucket + ".s3.amazonaws.com/" + directory + "/" + fileName;
    }

    public Map<String, String> getPrivateUploadParams(String fileName, String callback, String directory) {
        return getUploadParams(awsAccessKeyId, awsSecretAccessKey, bucket, fileName, callback, directory, maxSize, timeout, true, "");
    }

    public Map<String, String> getPrivateUploadParams(String fileName, String callback, String directory, String contentType) {
        return getUploadParams(awsAccessKeyId, awsSecretAccessKey, bucket, fileName, callback, directory, maxSize, timeout, true, contentType);
    }

    public Map<String, String> getPublicUploadParams(String fileName, String callback, String directory) {
        return getUploadParams(awsAccessKeyId, awsSecretAccessKey, bucket, fileName, callback, directory, maxSize, timeout, false, "");
    }

    public Map<String, String> getPublicUploadParams(String fileName, String callback, String directory, String contentType) {
        return getUploadParams(awsAccessKeyId, awsSecretAccessKey, bucket, fileName, callback, directory, maxSize, timeout, false, contentType);
    }

    public void uploadPrivateFile(String fileName, String directory, InputStream inputStream, String mimeType) {
        directUploadFile(bucket, fileName, directory, inputStream, mimeType, false, amazonS3);
    }

    public void uploadPublicFile(String fileName, String directory, InputStream inputStream, String mimeType) {
        directUploadFile(bucket, fileName, directory, inputStream, mimeType, true, amazonS3);
    }

    public void delete(String fileName, String directory) {
        deleteFile(bucket, fileName, directory, amazonS3);
    }

    public List<String> listObject(String directory) {
        List<String> keys = list(bucket, directory, amazonS3);
        return keys.stream().map(key -> getFileName(key)).collect(Collectors.toList());
    }
}


