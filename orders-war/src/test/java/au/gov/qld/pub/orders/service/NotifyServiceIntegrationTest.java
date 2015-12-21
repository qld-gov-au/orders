package au.gov.qld.pub.orders.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.List;

import org.apache.commons.collections.IteratorUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import au.gov.qld.pub.orders.ApplicationContextAwareTest;
import au.gov.qld.pub.orders.ProductProperties;
import au.gov.qld.pub.orders.dao.ItemDAO;
import au.gov.qld.pub.orders.dao.ItemPropertiesDAO;
import au.gov.qld.pub.orders.dao.OrderDAO;
import au.gov.qld.pub.orders.entity.Item;
import au.gov.qld.pub.orders.entity.Order;
import au.gov.qld.pub.orders.service.ws.OrderDetails;

import com.dumbster.smtp.SmtpMessage;
import com.google.common.collect.ImmutableMap;

public class NotifyServiceIntegrationTest extends ApplicationContextAwareTest {
    static final String RECEIPT = "some receipt";
    
    @Autowired OrderDAO orderDAO;
    @Autowired ItemDAO itemDAO;
    @Autowired ItemPropertiesDAO itemPropertiesDAO;
    @Autowired NotifyService service;
    Order order;
    Item item;    
    
    @Before
    public void setUp() {
        order = new Order(null);
        item = ProductProperties.populate(itemPropertiesDAO.find("test"));
        item.setFields(ImmutableMap.of("field1", "value1"));
        order.add(item);
        OrderDetails orderDetails = new OrderDetails();
        orderDetails.setCustomerDetails(ImmutableMap.of("email", "test@example.com"));
        orderDetails.setDeliveryDetails(ImmutableMap.of("deliverydetails1", "deliverydetailsvalue1"));
        orderDetails.setOrderlineQuantities(ImmutableMap.of(item.getId(), "1"));
        order.setPaid(RECEIPT, orderDetails);
        itemDAO.save(order.getItems());
        orderDAO.save(order);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void notifyAndSetOrderNotifiedForEachProductId() throws ServiceException, InterruptedException {
        service.send(order);
        
        Order saved = orderDAO.findOne(order.getId());
        assertThat(saved.getNotified(), notNullValue());
        
        List<SmtpMessage> messages = IteratorUtils.toList(mailServer.getReceivedEmail());
        assertThat(messages.size(), is(2));

        SmtpMessage business = messages.get(0);
        SmtpMessage customer = messages.get(1);
        assertThat(business.getHeaderValue("From"), is("noreply@www.qld.gov.au"));
        assertThat(business.getHeaderValue("Subject"), is("Test product has been purchased with receipt " + RECEIPT));
        assertThat(business.getBody(), containsString("business"));
        
        assertThat(customer.getHeaderValue("Subject"), is("Test product you purchased with receipt " + RECEIPT));
        assertThat(customer.getHeaderValue("From"), is("noreply@www.qld.gov.au"));
        assertThat(customer.getBody(), containsString("customer"));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void dontNotifyCustomerWhenEmailBlank() throws ServiceException, InterruptedException {
        OrderDetails orderDetails = new OrderDetails();
        orderDetails.setOrderlineQuantities(ImmutableMap.of(item.getId(), "1"));
        order.setPaid(RECEIPT, orderDetails);
        itemDAO.save(order.getItems());
        orderDAO.save(order);
        
        service.send(order);
        
        Order saved = orderDAO.findOne(order.getId());
        assertThat(saved.getNotified(), notNullValue());
        
        List<SmtpMessage> messages = IteratorUtils.toList(mailServer.getReceivedEmail());
        assertThat(messages.size(), is(1));
        SmtpMessage business = messages.get(0);
        assertThat(business.getHeaderValue("Subject"), is("Test product has been purchased with receipt " + RECEIPT));
        assertThat(business.getBody(), containsString("business"));
        assertThat(business.getBody(), containsString(RECEIPT));
    }
}
