package me.vcoder.s3helper;

import com.amazonaws.util.StringUtils;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * @author baodn
 * Created on 02 Apr 2018
 */
@SpringBootConfiguration
@EnableConfigurationProperties(value = {S3ServiceProperties.class})
public class S3ServiceConfiguration {
    @Bean(name = "singleBucketS3Service")
    public S3Service singleBucketS3Service(final S3ServiceProperties serviceProperties) {
        if(StringUtils.isNullOrEmpty(serviceProperties.getAccessKey()) ||
                StringUtils.isNullOrEmpty(serviceProperties.getSecretKey()) ||
                StringUtils.isNullOrEmpty(serviceProperties.getBucket())) {
            throw new IllegalArgumentException("S3Service is required external.s3.accessKey, external.s3.secretKey, external.s3.bucket");
        }
        return new S3Service(serviceProperties.getAccessKey(),
                serviceProperties.getSecretKey(),
                serviceProperties.getMaxSize(),
                serviceProperties.getTimeout(),
                serviceProperties.getBucket());
    }
}
