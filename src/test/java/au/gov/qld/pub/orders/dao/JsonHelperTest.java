package au.gov.qld.pub.orders.dao;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.util.Map;

import org.junit.Test;

import au.gov.qld.pub.orders.entity.ItemBuilder;
import au.gov.qld.pub.orders.entity.Order;

import com.google.common.collect.ImmutableMap;


public class JsonHelperTest {
	@Test
	public void serialiseMap() {
		Map<String, String> map = ImmutableMap.of("a", "a1", "b", "b1");
		String json = JsonHelper.serialise(map);
		assertThat(json, is("{\"a\":\"a1\",\"b\":\"b1\"}"));
	}
	
	@Test
	public void deserialiseBlankJsonToNull() {
		assertThat(JsonHelper.deserialise(Object.class, null), nullValue());
	}
	
	@Test
	public void serialise() {
		Order order = new Order("something");
		order.add(new ItemBuilder().withProductId("product id").build());
		String json = JsonHelper.serialise(order);
		
		Order deserialised = JsonHelper.deserialise(Order.class, json);
		assertThat(deserialised.getId(), is(order.getId()));
		assertThat(deserialised.getItems().get(0).getId(), is(order.getItems().get(0).getId()));
		assertThat(deserialised.getDeliveryDetailsMap(), is(order.getDeliveryDetailsMap()));
		assertThat(deserialised.getCustomerDetailsMap(), is(order.getCustomerDetailsMap()));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void deserialise() {
		Map<String, String> map = JsonHelper.deserialise(Map.class, "{\"a\":\"a1\",\"b\":\"b1\"}");
		assertThat(map, is((Map)ImmutableMap.of("a", "a1", "b", "b1")));
	}
}