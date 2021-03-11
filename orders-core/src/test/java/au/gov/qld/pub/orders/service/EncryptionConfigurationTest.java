package au.gov.qld.pub.orders.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;


public class EncryptionConfigurationTest {
    @Test
    public void loadEncryptionKey() throws Exception {
        EncryptionConfiguration configuration = new EncryptionConfiguration("/tmp/.orders.key", "testkey");
        assertThat(configuration.getPassword(), is("testkey"));
    }
}
