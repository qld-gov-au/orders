package au.gov.qld.pub.orders.dao;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.util.Properties;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import au.gov.qld.pub.orders.ApplicationContextAwareTest;

public class ItemDAOIntegrationTest extends ApplicationContextAwareTest {
    @Autowired ItemPropertiesDAO dao;
    
    @Test
    public void returnNullWhenUnknown() {
        assertThat(dao.find("bogus"), nullValue());
    }
    
    @Test
    public void returnNullWhenInvalidFormat() {
        assertThat(dao.find("../"), nullValue());
    }
    
    @Test
    public void returnPropertiesOfKnown() {
        Properties find = dao.find("test");
        assertThat((String)find.get("productId"), is("test"));
        assertThat((String)find.get("fields"), is("field1,field2"));
        assertThat((String)find.get("notifyBusinessEmail"), is("test@example.com"));
    }
    
    
}
