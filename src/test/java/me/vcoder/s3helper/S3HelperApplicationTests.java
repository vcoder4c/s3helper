package me.vcoder.s3helper;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileInputStream;
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration
public class S3HelperApplicationTests {
	private static final Logger LOGGER = LoggerFactory.getLogger(S3HelperApplicationTests.class);
	@Autowired S3Service s3Service;

	@Test
	public void testDirectUploadPublic() {
		try {
			Resource resource = new ClassPathResource("abc.jpg");
			String fileKey = UUID.randomUUID().toString();
			String directory = "test";
			String mimeType = "image/jpeg";
			s3Service.uploadPublicFile(fileKey, directory, resource.getInputStream(), mimeType);

			// check whether the file is uploaded
			String url = s3Service.getPublicAccessURL(fileKey, directory);
			LOGGER.info("Access URL:" + url);
			HttpUriRequest httpUriRequest = new HttpGet(url);
			HttpResponse response = HttpClientBuilder.create().build().execute(httpUriRequest);

			Assert.assertEquals(ContentType.getOrDefault(response.getEntity()).getMimeType(), mimeType);
			LOGGER.info("Mimetype: " + ContentType.getOrDefault(response.getEntity()).getMimeType());
			Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
			LOGGER.info("Status: " + response.getStatusLine().getStatusCode());
		} catch (Exception ex) {
			Assert.fail(ex.getMessage());
		}
	}

	@Test
	public void testDirectPrivateUpload() {
		try {
			Resource resource = new ClassPathResource("abc.jpg");
			String fileKey = UUID.randomUUID().toString();
			String directory = "test";
			String mimeType = "image/jpeg";
			s3Service.uploadPrivateFile(fileKey, directory, resource.getInputStream(), mimeType);

			// check whether the file is uploaded
			String url = s3Service.getPrivateAccessURL(fileKey, directory);
			LOGGER.info("Private Access URL:" + url);
			HttpUriRequest httpUriRequest = new HttpGet(url);
			HttpResponse response = HttpClientBuilder.create().build().execute(httpUriRequest);
			Assert.assertEquals(ContentType.getOrDefault(response.getEntity()).getMimeType(), mimeType);
			LOGGER.info("Mimetype: " + ContentType.getOrDefault(response.getEntity()).getMimeType());
			Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
			LOGGER.info("Status: " + response.getStatusLine().getStatusCode());

			// check whether the file is uploaded privately
			url = s3Service.getPublicAccessURL(fileKey, directory);
			LOGGER.info("Public Access URL:" + url);
			httpUriRequest = new HttpGet(url);
			response = HttpClientBuilder.create().build().execute(httpUriRequest);
			Assert.assertEquals(HttpStatus.SC_FORBIDDEN, response.getStatusLine().getStatusCode());
			LOGGER.info("Status: " + response.getStatusLine().getStatusCode());
		} catch (Exception ex) {
			Assert.fail(ex.getMessage());
		}
	}
}
