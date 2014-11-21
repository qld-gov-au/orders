package au.gov.qld.pub.orders.service;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

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
	public void byGroup() {
		Item itemA = new ItemBuilder().withGroup("a").build();
		Item itemB = new ItemBuilder().withGroup("b").build();
		
		Order order = new Order("cart id");
		order.setPaid("paid");
		order.setReceipt("receipt");
		order.add(itemA);
		order.add(itemB);
		
		Map<String, String> deliveryDetails = ImmutableMap.of("delivery field 1", "delivery value 1");
		Map<String, String> customerDetails = ImmutableMap.of("customer field 1", "customer value 1");
		order.setDeliveryDetailsMap(deliveryDetails);
		order.setCustomerDetailsMap(customerDetails);

		Map<String, Order> byGroup = orderGrouper.byProductGroup(order);
		Order productA = byGroup.get("a");
		Order productB = byGroup.get("b");
		
		assertThat(productA, notNullValue());
		assertThat(productB, notNullValue());
		assertThat(productA.getItems(), is(asList(itemA)));
		assertThat(productB.getItems(), is(asList(itemB)));
		assertThat(productA.getPaid(), is("paid"));
		assertThat(productB.getPaid(), is("paid"));
		assertThat(productA.getReceipt(), is("receipt"));
		assertThat(productB.getReceipt(), is("receipt"));
		
		assertThat(productA.getCustomerDetailsMap(), is(customerDetails));
		assertThat(productB.getDeliveryDetailsMap(), is(deliveryDetails));
		assertThat(productA.getCustomerDetailsMap(), is(customerDetails));
		assertThat(productB.getDeliveryDetailsMap(), is(deliveryDetails));
	}
}
