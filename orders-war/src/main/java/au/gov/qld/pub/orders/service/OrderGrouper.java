package au.gov.qld.pub.orders.service;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import au.gov.qld.pub.orders.entity.Item;
import au.gov.qld.pub.orders.entity.Order;

@Component
public class OrderGrouper {

    public Map<String, Order> paidByProductGroup(Order order) {
        Map<String, Order> grouped = new HashMap<>();
        for (Item item : order.getPaidItems()) {
            String group = item.getProductGroup();
            Order orderForGroup = grouped.containsKey(group) ? grouped.get(group) : new Order(order.getCartId());
            orderForGroup.setId(order.getId());
            orderForGroup.setReceipt(order.getReceipt());
            orderForGroup.setPaid(order.getPaid());
            orderForGroup.setDeliveryDetailsMap(order.getDeliveryDetailsMap());
            orderForGroup.setCustomerDetailsMap(order.getCustomerDetailsMap());
            orderForGroup.setCreatedAt(new Date(order.getCreated().getTime()));
            orderForGroup.setGeneratedId(order.getGeneratedId());
            orderForGroup.setNotified(order.getNotified());
            
            orderForGroup.add(item);
            grouped.put(group, orderForGroup);
        }
        return Collections.unmodifiableMap(grouped);
    }
}
