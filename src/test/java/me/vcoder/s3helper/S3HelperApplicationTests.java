package me.vcoder.s3helper;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration
public class S3HelperApplicationTests {
	private static final Logger LOGGER = LoggerFactory.getLogger(S3HelperApplicationTests.class);
	private static final String DIRECTORY = "test";
	private static final String MIMETYPE = "image/jpeg";
	@Autowired
	S3Service s3Service;

	private String uploadDirectlyAPrivateFile() throws IOException {
		Resource resource = new ClassPathResource("abc.jpg");
		String fileKey = UUID.randomUUID().toString();
		s3Service.uploadPrivateFile(fileKey, DIRECTORY, resource.getInputStream(), MIMETYPE);

		return fileKey;
	}

	private String uploadDirectlyAPublicFile() throws IOException {
		Resource resource = new ClassPathResource("abc.jpg");
		String fileKey = UUID.randomUUID().toString();
		s3Service.uploadPublicFile(fileKey, DIRECTORY, resource.getInputStream(), MIMETYPE);

		return fileKey;
	}

	@Before
	public void setup() {
		List<String> files = s3Service.listObject(DIRECTORY);
		LOGGER.info("Setup found " + files.size() + " file. Need to delete all of them");
		files.forEach(file -> s3Service.delete(file, DIRECTORY));
	}

	@Test
	public void testDirectUploadPublic() {
		try {
			String fileKey = uploadDirectlyAPublicFile();

			// check whether the file is uploaded
			String url = s3Service.getPublicAccessURL(fileKey, DIRECTORY);
			LOGGER.info("Access URL:" + url);
			HttpUriRequest httpUriRequest = new HttpGet(url);
			HttpResponse response = HttpClientBuilder.create().build().execute(httpUriRequest);

			Assert.assertEquals(ContentType.getOrDefault(response.getEntity()).getMimeType(), MIMETYPE);
			LOGGER.info("Mimetype: " + ContentType.getOrDefault(response.getEntity()).getMimeType());
			Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
			LOGGER.info("Status: " + response.getStatusLine().getStatusCode());

			// delete file
			s3Service.delete(fileKey, DIRECTORY);
			response = HttpClientBuilder.create().build().execute(httpUriRequest);
			Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_FORBIDDEN);
			LOGGER.info("Status: " + response.getStatusLine().getStatusCode());
			// check whethere the file is deleted
			Assert.assertEquals(HttpStatus.SC_FORBIDDEN, response.getStatusLine().getStatusCode());
		} catch (Exception ex) {
			Assert.fail(ex.getMessage());
		}
	}

	@Test
	public void testDirectPrivateUpload() {
		try {
			String fileKey = uploadDirectlyAPrivateFile();

			// check whether the file is uploaded
			String url = s3Service.getPrivateAccessURL(fileKey, DIRECTORY);
			LOGGER.info("Private Access URL:" + url);
			HttpUriRequest httpUriRequest = new HttpGet(url);
			HttpResponse response = HttpClientBuilder.create().build().execute(httpUriRequest);
			Assert.assertEquals(ContentType.getOrDefault(response.getEntity()).getMimeType(), MIMETYPE);
			LOGGER.info("Mimetype: " + ContentType.getOrDefault(response.getEntity()).getMimeType());
			Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
			LOGGER.info("Status: " + response.getStatusLine().getStatusCode());

			// check whether the file is uploaded privately
			url = s3Service.getPublicAccessURL(fileKey, DIRECTORY);
			LOGGER.info("Public Access URL:" + url);
			httpUriRequest = new HttpGet(url);
			response = HttpClientBuilder.create().build().execute(httpUriRequest);
			Assert.assertEquals(HttpStatus.SC_FORBIDDEN, response.getStatusLine().getStatusCode());

			// delete file
			s3Service.delete(fileKey, DIRECTORY);
			response = HttpClientBuilder.create().build().execute(httpUriRequest);
			Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_FORBIDDEN);
			LOGGER.info("Status: " + response.getStatusLine().getStatusCode());
			// check whethere the file is deleted
			Assert.assertEquals(HttpStatus.SC_FORBIDDEN, response.getStatusLine().getStatusCode());
		} catch (Exception ex) {
			Assert.fail(ex.getMessage());
		}
	}

	@Test
	public void testListObject() {
		try {
			// upload ten files
			List<String> files = new ArrayList<>();
			for(int i = 0; i < 10; i++) {
				files.add(uploadDirectlyAPublicFile());
			}

			List<String> keys = s3Service.listObject(DIRECTORY);
			Assert.assertEquals(10, keys.size());

			// delete file keys
			keys.forEach(key -> files.remove(key));
			Assert.assertEquals(0, files.size());

			// remove on S3
			keys.forEach(key -> s3Service.delete(key, DIRECTORY));
			keys = s3Service.listObject("test");
			Assert.assertTrue(keys.size() == 0);
		} catch (IOException ex) {
			Assert.fail(ex.getMessage());
		}
	}

	@After
	public void cleanup() {
		LOGGER.info("Clean up the TEST DIRECTORY");
		List<String> files = s3Service.listObject(DIRECTORY);
		files.forEach(file -> s3Service.delete(file, DIRECTORY));
	}
}
