package me.vcoder.s3helper;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.InputStream;
import java.util.*;

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
        this.amazonS3 =  new AmazonS3Client(awsCreds);

        LOGGER.info("Initialize S3Service successfully");
    }

    public String getPrivateAccessURL(String fileKey, String directory) {
        return getAccessURL(bucket, fileKey, directory, amazonS3, timeout);
    }

    public String getPublicAccessURL(String fileKey, String directory) {
        return "https://" + bucket + ".s3.amazonaws.com/" + directory + "/" + fileKey;
    }

    public Map<String, String> getPrivateUploadParams(String fileKey, String callback, String directory) {
        return getUploadParams(awsAccessKeyId, awsSecretAccessKey, bucket, fileKey, callback, directory, maxSize, timeout, true, "");
    }

    public Map<String, String> getPrivateUploadParams(String fileKey, String callback, String directory, String contentType) {
        return getUploadParams(awsAccessKeyId, awsSecretAccessKey, bucket, fileKey, callback, directory, maxSize, timeout, true, contentType);
    }

    public Map<String, String> getPublicUploadParams(String fileKey, String callback, String directory) {
        return getUploadParams(awsAccessKeyId, awsSecretAccessKey, bucket, fileKey, callback, directory, maxSize, timeout, false, "");
    }

    public Map<String, String> getPublicUploadParams(String fileKey, String callback, String directory, String contentType) {
        return getUploadParams(awsAccessKeyId, awsSecretAccessKey, bucket, fileKey, callback, directory, maxSize, timeout, false, contentType);
    }

    public void uploadPrivateFile(String fileKey, String directory, InputStream inputStream, String mimeType) {
        directUploadFile(bucket, fileKey, directory, inputStream, mimeType, false, amazonS3);
    }

    public void uploadPublicFile(String fileKey, String directory, InputStream inputStream, String mimeType) {
        directUploadFile(bucket, fileKey, directory, inputStream, mimeType, true, amazonS3);
    }
}


