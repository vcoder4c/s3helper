package me.vcoder.s3helper;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author baodn
 * Created on 02 Apr 2018
 */
public abstract class GeneralS3Service {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(GeneralS3Service.class);
    private static final String $EXPIRATION = "$expiration";
    private static final String $BUCKET = "$bucket";
    private static final String $ACL = "$acl";
    private static final String $SUCCESS_ACTION_REDIRECT = "$success_action_redirect";
    private static final String $CONTENT_LENGTH = "$contentLength";
    private static final String $CONTENT_TYPE = "$contentType";
    private static final String ACL = "acl";
    private static final String PRIVATE = "private";
    private static final String PUBLIC = "public-read";
    private static final String KEY = "key";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String SUCCESS_ACTION_REDIRECT = "success_action_redirect";
    private static final String UTF_8 = "UTF-8";
    private static final String AWS_ACCESS_KEY_ID = "AWSAccessKeyId";
    private static final String POLICY = "policy";
    private static final String SIGNATURE = "signature";
    private static final String HMAC_SHA_1 = "HmacSHA1";
    private static final String URL = "url";
    private static final String $S3KEY = "$key";

    public String getAccessURL(String bucket, String fileKey, String directory, AmazonS3Client amazonS3, int timeout) {
        GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(
                bucket, joinPath(directory, fileKey));
        generatePresignedUrlRequest.setMethod(HttpMethod.GET);
        generatePresignedUrlRequest.setExpiration(getAmazonS3Expiration(timeout));
        generatePresignedUrlRequest.setBucketName(bucket);
        java.net.URL s = amazonS3.generatePresignedUrl(generatePresignedUrlRequest);

        return s.toString();
    }

    public Map<String, String> getUploadParams(String awsAccessKeyId, String awsSecretAccessKey, String bucket, String fileKey, String callback, String directory, int maxSize, int timeout, boolean isPrivate, String contentType) {
        Map<String, String> formFields = null;
        if(isPrivate) {
            formFields = makeFormFields(
                    joinPath(directory, fileKey), PRIVATE, callback, contentType);
        } else {
            formFields = makeFormFields(
                    joinPath(directory, fileKey), PUBLIC, callback, contentType);
        }
        String policy = null;
        try {
            policy = makePolicy(formFields, bucket, maxSize, timeout);
        } catch (UnsupportedEncodingException e) {
        }
        String signature = null;
        try {
            signature = createSignature(policy, awsSecretAccessKey);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException
                | InvalidKeyException e) {
        }
        formFields.put(AWS_ACCESS_KEY_ID, awsAccessKeyId);
        formFields.put(POLICY, policy);
        formFields.put(SIGNATURE, signature);
        formFields.put(URL, "https://" + bucket + ".s3.amazonaws.com");
        return formFields;
    }

    private String createSignature(String policy, String awsSecretAccessKey)
            throws NoSuchAlgorithmException, UnsupportedEncodingException,
            InvalidKeyException {
        Mac hmac = Mac.getInstance(HMAC_SHA_1);
        hmac.init(new SecretKeySpec(awsSecretAccessKey.getBytes(UTF_8),
                HMAC_SHA_1));
        String signature = Base64.getMimeEncoder().encodeToString(
                hmac.doFinal(policy.getBytes(UTF_8))).replaceAll("\n", "");
        return signature;
    }

    private String makePolicy(Map<String, String> formFields, String bucket, int maxSize, int timeout)
            throws UnsupportedEncodingException {
        String policyDocument = "{\"expiration\": \"" + $EXPIRATION + "\","
                + "\"conditions\": [" +
                "{\"bucket\": \"" + $BUCKET + "\"},"
                + "[\"starts-with\", \"$KEY\", \"" + $S3KEY + "\"],"
                + "{\"acl\": \"" + $ACL + "\"},"
                + "{\"success_action_redirect\": \"" + $SUCCESS_ACTION_REDIRECT + "\"},"
                + "[\"content-length-range\", 0, " + $CONTENT_LENGTH + "],"
                + "[\"starts-with\", \"$Content-Type\", \""+ $CONTENT_TYPE + "\"]"
                + "]"
                + "}";
        String date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
                .format(getAmazonS3Expiration(timeout));
        policyDocument = policyDocument
                .replace($EXPIRATION, date)
                .replace($BUCKET, bucket)
                .replace($ACL, formFields.get(ACL))
                .replace($SUCCESS_ACTION_REDIRECT,
                        formFields.get(SUCCESS_ACTION_REDIRECT))
                .replace($CONTENT_LENGTH, String.valueOf(maxSize))
                .replace($S3KEY, formFields.get(KEY))
                .replace($CONTENT_TYPE, formFields.get(CONTENT_TYPE));
        LOGGER.info("policy Document:" + policyDocument);
        String policy = Base64.getMimeEncoder()
                .encodeToString(policyDocument.getBytes(UTF_8)).replaceAll("\n", "")
                .replaceAll("\r", "");
        return policy;
    }

    private Map<String, String> makeFormFields(String s3Key, String acl, String callback, String contentType) {
        Map<String, String> formFields = new HashMap<>();
        formFields.put(KEY, s3Key);
        formFields.put(ACL, acl);
        formFields.put(SUCCESS_ACTION_REDIRECT, callback);
        formFields.put(CONTENT_TYPE, contentType);
        return formFields;
    }

    private String joinPath(String basePath, String subPath) {
        String path = basePath;
        if (!path.endsWith("/")) {
            path += "/";
        }
        if (subPath.startsWith("/")) {
            subPath = subPath.substring(1);
        }
        path += subPath;
        return path;
    }

    private Date getAmazonS3Expiration(int timeout) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, timeout * 60);
        return cal.getTime();
    }
}
