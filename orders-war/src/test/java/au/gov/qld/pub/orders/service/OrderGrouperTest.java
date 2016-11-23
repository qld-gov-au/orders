package au.gov.qld.pub.orders.service;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.util.Date;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import au.gov.qld.pub.orders.entity.Item;
import au.gov.qld.pub.orders.entity.ItemBuilder;
import au.gov.qld.pub.orders.entity.Order;

import com.google.common.collect.ImmutableMap;


public class OrderGrouperTest {
    OrderGrouper orderGrouper;
    
    @Before
    public void setUp() {
        orderGrouper = new OrderGrouper();
    }
    
    @Test
    public void paidByGroup() {
        Item itemA = new ItemBuilder().withGroup("a").withPaid("1").build();
        Item itemB = new ItemBuilder().withGroup("b").withPaid("1").build();
        Item itemBUnpaid = new ItemBuilder().withGroup("b").withPaid("0").build();
        Item itemC = new ItemBuilder().withGroup("c").withPaid("0").build();
        
        Date paidAt = new Date();
        Order order = new Order("cart id");
        order.setPaidAt(paidAt);
        order.setPaid("paid");
        order.setReceipt("receipt");
        order.add(itemA);
        order.add(itemB);
        order.add(itemBUnpaid);
        order.add(itemC);
        
        Map<String, String> deliveryDetails = ImmutableMap.of("delivery field 1", "delivery value 1");
        Map<String, String> customerDetails = ImmutableMap.of("customer field 1", "customer value 1");
        order.setDeliveryDetailsMap(deliveryDetails);
        order.setCustomerDetailsMap(customerDetails);

        Map<String, Order> byGroup = orderGrouper.paidByProductGroup(order);
        Order productA = byGroup.get("a");
        assertThat(productA.getId(), is(order.getId()));
        Order productB = byGroup.get("b");
        assertThat(productB.getId(), is(order.getId()));
        assertThat(byGroup.get("c"), nullValue());
        
        assertThat(productA, notNullValue());
        assertThat(productB, notNullValue());
        assertThat(productA.getItems(), is(asList(itemA)));
        assertThat(productB.getItems(), is(asList(itemB)));
        assertThat(productA.getPaid(), is("paid"));
        assertThat(productA.getPaidAt(), is(paidAt));
        assertThat(productB.getPaid(), is("paid"));
        assertThat(productB.getPaidAt(), is(paidAt));
        assertThat(productA.getReceipt(), is("receipt"));
        assertThat(productB.getReceipt(), is("receipt"));
        
        assertThat(productA.getCustomerDetailsMap(), is(customerDetails));
        assertThat(productB.getDeliveryDetailsMap(), is(deliveryDetails));
        assertThat(productA.getCustomerDetailsMap(), is(customerDetails));
        assertThat(productB.getDeliveryDetailsMap(), is(deliveryDetails));
    }
}
