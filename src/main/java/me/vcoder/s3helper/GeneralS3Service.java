package me.vcoder.s3helper;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

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

    /**
     * Get the access URL for a file
     * @param bucket
     * @param fileName
     * @param directory
     * @param amazonS3
     * @param timeout
     * @return
     */
    protected String getAccessURL(String bucket, String fileName, String directory, AmazonS3Client amazonS3, int timeout) {
        GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(
                bucket, joinPath(directory, fileName));
        generatePresignedUrlRequest.setMethod(HttpMethod.GET);
        generatePresignedUrlRequest.setExpiration(getAmazonS3Expiration(timeout));
        generatePresignedUrlRequest.setBucketName(bucket);
        java.net.URL s = amazonS3.generatePresignedUrl(generatePresignedUrlRequest);

        return s.toString();
    }

    /**
     * Get the upload form for client side to help them can upload directly to S3 securely
     * @param awsAccessKeyId
     * @param awsSecretAccessKey
     * @param bucket
     * @param fileKey
     * @param callback
     * @param directory
     * @param maxSize
     * @param timeout
     * @param isPrivate
     * @param contentType
     * @return
     */
    protected Map<String, String> getUploadParams(String awsAccessKeyId, String awsSecretAccessKey, String bucket, String fileKey, String callback, String directory, int maxSize, int timeout, boolean isPrivate, String contentType) {
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

    /**
     * Directly upload to S3
     * @param bucket
     * @param fileName
     * @param directory
     * @param inputStream
     * @param mimeType
     * @param isPublic
     * @param amazonS3
     */
    protected void directUploadFile(String bucket, String fileName, String directory, InputStream inputStream, String mimeType, boolean isPublic, AmazonS3Client amazonS3) {
        String key = joinPath(directory, fileName);
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(mimeType);
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, key, inputStream, objectMetadata);
        if(isPublic) {
            putObjectRequest.setCannedAcl(CannedAccessControlList.PublicRead);
        } else {
            putObjectRequest.setCannedAcl(CannedAccessControlList.Private);
        }
        amazonS3.putObject(putObjectRequest);
    }

    /**
     * Delete a file with filename and its directory
     * @param bucket
     * @param fileName
     * @param directory
     * @param amazonS3
     */
    protected void deleteFile(String bucket, String fileName, String directory, AmazonS3Client amazonS3) {
        DeleteObjectRequest request = new DeleteObjectRequest(bucket, joinPath(directory, fileName));
        amazonS3.deleteObject(request);
    }

    /**
     * Retrieve a list of file key on a bucket and directory
     * @param bucket
     * @param directory
     * @param amazonS3
     * @return List of file key (file key contains directory)
     */
    protected List<String> list(String bucket, String directory, AmazonS3Client amazonS3) {
        ListObjectsRequest listObjectsRequest = new ListObjectsRequest();
        listObjectsRequest.setBucketName(bucket);
        listObjectsRequest.setPrefix(directory + "/");

        ObjectListing listing = amazonS3.listObjects(listObjectsRequest);
        List<S3ObjectSummary> objectSummaries = listing.getObjectSummaries();
        return objectSummaries.stream().map(S3ObjectSummary::getKey).collect(Collectors.toList());
    }

    /**
     * Create signature
     * @param policy
     * @param awsSecretAccessKey
     * @return Signature as String
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     * @throws InvalidKeyException
     */
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

    /**
     * Make policy with specific bucket, max size, timeout constraint
     * @param formFields
     * @param bucket
     * @param maxSize
     * @param timeout
     * @return
     * @throws UnsupportedEncodingException
     */
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
        String policy = Base64.getMimeEncoder()
                .encodeToString(policyDocument.getBytes(UTF_8)).replaceAll("\n", "")
                .replaceAll("\r", "");
        return policy;
    }

    /**
     * Create the form for uploading. This form is used by client side to help them can upload to S3 directly
     * @param s3Key
     * @param acl
     * @param callback
     * @param contentType
     * @return
     */
    private Map<String, String> makeFormFields(String s3Key, String acl, String callback, String contentType) {
        Map<String, String> formFields = new HashMap<>();
        formFields.put(KEY, s3Key);
        formFields.put(ACL, acl);
        formFields.put(SUCCESS_ACTION_REDIRECT, callback);
        formFields.put(CONTENT_TYPE, contentType);
        return formFields;
    }

    /**
     * Join two path
     * @param basePath
     * @param subPath
     * @return
     */
    protected String joinPath(String basePath, String subPath) {
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

    /**
     * Extract file name from file key.
     * Ex: /test/abc.png will return abc.png
     * @param fileKey
     * @return filename
     */
    protected String getFileName(String fileKey) {
        String[] tmp = fileKey.split("/");
        return tmp[tmp.length - 1];
    }

    /**
     * Return a date by adding timeout (seconds) to the current date
     * @param timeout
     * @return expired date
     */
    private Date getAmazonS3Expiration(int timeout) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND, timeout);
        return cal.getTime();
    }
}
