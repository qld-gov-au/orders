package au.gov.qld.pub.orders.dao;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.util.Map;
import java.util.UUID;

import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import au.gov.qld.pub.orders.ApplicationContextAwareTest;
import au.gov.qld.pub.orders.ProductProperties;
import au.gov.qld.pub.orders.entity.Item;
import au.gov.qld.pub.orders.entity.Order;
import au.gov.qld.pub.orders.service.ws.OrderDetails;

import com.google.common.collect.ImmutableMap;

public class OrderDAOIntegrationTest extends ApplicationContextAwareTest {
    @Autowired OrderDAO dao;
    @Autowired ItemDAO itemDAO;
    @Autowired ItemPropertiesDAO itemPropertiesDAO;
    
    @Test
    public void saveAndFind() {
        String cartId = UUID.randomUUID().toString();
        
        Order order = new Order(cartId);
        Item item = ProductProperties.populate(itemPropertiesDAO.find("test"));
        Map<String, String> fields = ImmutableMap.of("field1", "value1", "field2", "value2");
        item.setFields(fields);
        itemDAO.save(item);
        order.add(item);
        
        String id = order.getId();
        dao.save(order);
        
        Order findByCartId = dao.findByCartId(cartId);
        assertThat(findByCartId, notNullValue());
        assertThat(findByCartId.getId(), is(id));
        assertThat(findByCartId.getCartId(), is(cartId));
        assertThat(findByCartId.getItems().size(), is(1));
        assertThat(findByCartId.getItems().get(0).getId(), is(item.getId()));
        assertThat(findByCartId.getItems().get(0).getFieldsMap(), is(fields));
        
        Order findByOrderId = dao.findOne(id);
        assertThat(findByOrderId, notNullValue());
        assertThat(findByOrderId.getId(), is(id));
        assertThat(findByOrderId.getCartId(), is(cartId));
        assertThat(findByOrderId.getItems().size(), is(1));
        assertThat(findByOrderId.getItems().get(0).getId(), is(item.getId()));
        assertThat(findByOrderId.getItems().get(0).getFieldsMap(), is(fields));
        
        OrderDetails orderDetails = new OrderDetails();
        orderDetails.setCustomerDetails(ImmutableMap.of("customer type", "customer detail"));
        orderDetails.setDeliveryDetails(ImmutableMap.of("delivery type", "delivery detail"));
        orderDetails.setOrderlineQuantities(ImmutableMap.of(item.getId(), "2"));
        findByOrderId.setPaid("some receipt", orderDetails);
        
        dao.save(findByOrderId);
        findByOrderId = dao.findOne(id);
        assertThat(findByOrderId, notNullValue());
        assertThat(findByOrderId.getReceipt(), is("some receipt"));
        assertThat(findByOrderId.getCustomerDetailsMap().get("customer type"), is("customer detail"));
        assertThat(findByOrderId.getDeliveryDetailsMap().get("delivery type"), is("delivery detail"));
        assertThat(findByOrderId.getItems().get(0).getQuantityPaid(), is("2"));
        assertThat(findByOrderId.getPaid(), notNullValue());
    }
    
    @Test
    public void dontSaveCartIdWhenNull() {
        Order order = new Order(null);
        String id = order.getId();
        order.add(ProductProperties.populate(itemPropertiesDAO.find("test")));
        itemDAO.save(order.getItems());
        dao.save(order);
        
        Order findByCartId = dao.findByCartId("null");
        assertThat(findByCartId, nullValue());
        
        Order findByOrderId = dao.findOne(id);
        assertThat(findByOrderId, notNullValue());
        assertThat(findByOrderId.getId(), is(id));
    }
    
    @Test
    public void findUnpaidOrdersCreatedAfterMinDate() {
        Order unpaidOrder = new Order(UUID.randomUUID().toString());
        String unpaidId = unpaidOrder.getId();
        dao.save(unpaidOrder);
        
        Order paidOrder = new Order(UUID.randomUUID().toString());
        paidOrder.setPaid("some paid");
        String paidId = paidOrder.getId();
        dao.save(paidOrder);
        
        
        Iterable<String> unpaidIds = dao.findUnpaidOrdersCreatedAfter(new LocalDateTime().minusHours(1).toDate());
        assertThat(unpaidIds, hasItem(unpaidId));
        assertThat(unpaidIds, not(hasItem(paidId)));
    }
}
