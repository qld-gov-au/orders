package au.gov.qld.pub.orders.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.IOException;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import au.gov.qld.pub.orders.ApplicationContextAwareTest;
import au.gov.qld.pub.orders.entity.ItemProperties;

public class DatabaseItemPropertiesServiceIntegrationTest extends ApplicationContextAwareTest {
    @Autowired DatabaseItemPropertiesService service;
    
    @Test
    public void returnTemplatedItemFromProperties() throws IOException {
    	ItemProperties find = service.find("test");
    	assertThat(find.getProductId(), is("test"));
    	assertThat(find.getProductGroup(), is("testgroup"));
    }
}
