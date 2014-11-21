package au.gov.qld.pub.orders.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import au.gov.qld.pub.orders.entity.Item;
import au.gov.qld.pub.orders.entity.Order;

@Component
public class OrderGrouper {

	public Map<String, Order> byProductGroup(Order order) {
		Map<String, Order> grouped = new HashMap<String, Order>();
		for (Item item : order.getItems()) {
			String group = item.getProductGroup();
			
			final Order orderForGroup;
			if (grouped.containsKey(group)) {
				orderForGroup = grouped.get(group);
			} else {
				orderForGroup = new Order(order.getCartId());
			}
			
			orderForGroup.setReceipt(order.getReceipt());
			orderForGroup.setPaid(order.getPaid());
			orderForGroup.setDeliveryDetailsMap(order.getDeliveryDetailsMap());
			orderForGroup.setCustomerDetailsMap(order.getCustomerDetailsMap());
			orderForGroup.add(item);
			grouped.put(group, orderForGroup);
		}
		return grouped;
	}
}
