package au.gov.qld.pub.orders.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Test;


public class EncryptionConfigurationTest {
	@Test
	public void loadEncryptionKey() throws Exception {
		String location = System.getProperty("user.home") + File.separator + ".orders.key";
		File keyFile = new File(location);
		if (!keyFile.exists()) {
			System.err.println("Creating test key");
			FileUtils.write(keyFile, "testkey");
		}
		
		EncryptionConfiguration configuration = new EncryptionConfiguration();
		assertThat(configuration.getPassword(), is("testkey"));
	}
}
