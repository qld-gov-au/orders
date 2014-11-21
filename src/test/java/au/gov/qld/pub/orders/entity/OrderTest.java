package au.gov.qld.pub.orders.entity;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import au.gov.qld.pub.orders.service.ws.OrderDetails;

import com.google.common.collect.ImmutableMap;

@RunWith(MockitoJUnitRunner.class)
public class OrderTest {
	@Mock Item item1;
	@Mock Item item2;
	@Mock Item item3;
	@Mock OrderDetails orderDetails;
	Map<String, String> deliveryDetails = ImmutableMap.of("delivery", "delivery details");
	Map<String, String> customerDetails = ImmutableMap.of("customer", "customer details");
	private Order order;
	
	@Before
	public void setUp() {
		when(orderDetails.getDeliveryDetails()).thenReturn(deliveryDetails);
		when(orderDetails.getCustomerDetails()).thenReturn(customerDetails);
		when(item1.getId()).thenReturn("a");
		when(item2.getId()).thenReturn("b");
		when(item3.getId()).thenReturn("c");
		
		order = new Order("anything");
		order.add(item1);
		order.add(item2);
		order.add(item3);
	}
	
	@Test(expected = IllegalStateException.class)
	public void throwExceptionWhenNoReceiptAndSettingPaid() {
		order.setPaid(null, null);
	}
	
	@Test
	public void setPaidAndDetails() {
		when(orderDetails.getOrderlineQuantities()).thenReturn(ImmutableMap.of("a", "1", "c", "2"));
		
		order.setPaid("receipt", orderDetails);
		assertThat(order.getReceipt(), is("receipt"));
		assertThat(order.getPaid(), notNullValue());
		
		assertThat(order.getCustomerDetailsMap(), is(customerDetails));
		assertThat(order.getDeliveryDetailsMap(), is(deliveryDetails));
		verify(item1).setQuantityPaid("1");
		verify(item2).setQuantityPaid(null);
		verify(item3).setQuantityPaid("2");
	}
}

