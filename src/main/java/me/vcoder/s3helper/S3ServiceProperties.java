package me.vcoder.s3helper;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author baodn
 * Created on 02 Apr 2018
 */
@ConfigurationProperties(prefix = "external.s3")
public class S3ServiceProperties {
    private String accessKey;
    private String secretKey;
    private Integer maxSize;
    private Integer timeout;
    private String bucket;

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public Integer getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(Integer maxSize) {
        this.maxSize = maxSize;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    @Override
    public String toString() {
        return "S3ServiceProperties{" +
                "accessKey='" + accessKey + '\'' +
                ", secretKey='" + secretKey + '\'' +
                ", maxSize=" + maxSize +
                ", bucket='" + bucket + '\'' +
                '}';
    }
}
