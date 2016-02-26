package au.gov.qld.pub.orders.service.ws;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import au.gov.qld.pub.orders.ApplicationContextAwareTest;
import au.gov.qld.pub.orders.dao.ItemPropertiesDAO;
import au.gov.qld.pub.orders.entity.Item;
import au.gov.qld.pub.orders.service.ws.TemplateItemBuilder.TemplateItem;

import com.google.common.collect.ImmutableMap;


public class TemplateItemBuilderTest extends ApplicationContextAwareTest {
    @Autowired ItemPropertiesDAO itemPropertiesDAO;
    @Autowired TemplateItemBuilder builder;

    @Test
    public void freemarkerTemplatedTitleAndDescription() {
        Item item = Item.createItem(itemPropertiesDAO.findOne("test"));
        item.setFields(ImmutableMap.of("field1", "value1", "field2", "value2"));
        
        List<TemplateItem> templated = builder.build(asList(item));
        TemplateItem templateItem = templated.get(0);
        
        assertThat(templateItem.getAgency(), is(item.getAgency()));
        assertThat(templateItem.getCartState(), is(item.getCartState()));
        assertThat(templateItem.getCustomerDetailsRequired(), is(item.getCustomerDetailsRequired()));
        assertThat(templateItem.getDeliveryDetailsRequired(), is(item.getDeliveryDetailsRequired()));
        assertThat(templateItem.getFieldsMap(), is(item.getFieldsMap()));
        
        assertThat(templateItem.getTitle(), is("test title value1"));
        assertThat(templateItem.getDescription(), is("test description value2"));
    }
}
