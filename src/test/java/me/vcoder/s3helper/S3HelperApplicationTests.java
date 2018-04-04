package me.vcoder.s3helper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration
public class S3HelperApplicationTests {
	private static final Logger LOGGER = LoggerFactory.getLogger(S3HelperApplicationTests.class);
	@Autowired S3Service s3Service;

	@Test
	public void contextLoads() {
        Map<String, String> params = s3Service.getPublicUploadParams("abc.png", "", "logo");
	}
}
