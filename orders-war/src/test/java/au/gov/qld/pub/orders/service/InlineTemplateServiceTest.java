package au.gov.qld.pub.orders.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Before;
import org.junit.Test;

import au.gov.qld.pub.orders.entity.Item;
import au.gov.qld.pub.orders.entity.ItemBuilder;


public class InlineTemplateServiceTest {
    InlineTemplateService service;
    
    @Before
    public void setUp() {
        service = new InlineTemplateService();
    }
    
    @Test
    public void returnTemplatedString() {
        Item item = new ItemBuilder().build();
        String result = service.template("anything", "${productId}", item);
        assertThat(result, is(item.getProductId()));
    }
    
    @Test(expected = IllegalStateException.class)
    public void throwRuntimeExceptionOnException() {
        service.template("anything", "${productId}", null);
    }
}
