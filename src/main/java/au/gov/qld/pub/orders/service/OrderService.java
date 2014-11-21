package au.gov.qld.bdm.orders.service;

import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import au.gov.qld.bdm.orders.dao.ItemDAO;
import au.gov.qld.bdm.orders.dao.ItemPropertiesDAO;
import au.gov.qld.bdm.orders.dao.OrderDAO;
import au.gov.qld.bdm.orders.entity.CartState;
import au.gov.qld.bdm.orders.entity.Item;
import au.gov.qld.bdm.orders.entity.Order;
import au.gov.qld.bdm.orders.service.ws.CartResponseParser;
import au.gov.qld.bdm.orders.service.ws.CartService;
import au.gov.qld.bdm.orders.service.ws.OrderDetails;
import au.gov.qld.bdm.orders.service.ws.RequestBuilder;

@Service
public class OrderService {
	private static final Logger LOG = LoggerFactory.getLogger(OrderService.class);
	
	private static final Pattern CART_ID_PATTERN = Pattern.compile("<cartId>(.+)</cartId>");
	private static final Pattern GENERATED_ORDER_ID_PATTERN = Pattern.compile("<generatedOrderId>(.+)</generatedOrderId>");
	
	private final CartService cartService;
	private final OrderDAO orderDAO;
	private final ItemPropertiesDAO itemPropertiesDAO;
	private final ItemDAO itemDAO;
	private final RequestBuilder requestBuilder;
	private final CartResponseParser responseParser;

	@Autowired
	public OrderService(CartService cartService, OrderDAO orderDAO, ItemDAO itemDAO, ItemPropertiesDAO itemPropertiesDAO,
			RequestBuilder requestBuilder, CartResponseParser responseParser) {
		this.cartService = cartService;
		this.orderDAO = orderDAO;
		this.itemDAO = itemDAO;
		this.itemPropertiesDAO = itemPropertiesDAO;
		this.requestBuilder = requestBuilder;
		this.responseParser = responseParser;
	}
	
	@Transactional
	public Order add(List<Item> items, String cartId) throws ServiceException {
		Order order = findByCartId(cartId);
		if (order == null) {
			LOG.info("Creating new order for cartId: {}", cartId);
			order = new Order(cartId);
		} else if (isNotBlank(order.getPaid())) {
			LOG.info("Returning customer, creating new order for paid cart id {} which will get a new cartId", cartId);
			order = new Order(null);
		}
		
		for (Item item : items) {
			order.add(item);
		}
		itemDAO.save(order.getItems());
		orderDAO.save(order);
		
		String addRequest = requestBuilder.addRequest(order);
		LOG.info("Sending cart add request for order: {} with cartId: {}", order.getId(), order.getCartId());
		String response = cartService.addToCart(addRequest);
		LOG.debug("Cart add response: {}", response);
		for (Item item : order.getItems()) {
			item.setCartState(CartState.ADDED);
		}
		
		String responseCartId = getValueFrom(CART_ID_PATTERN, response);
		String responseGeneratedId = getValueFrom(GENERATED_ORDER_ID_PATTERN, response);
		order.setCartId(responseCartId);
		order.setGeneratedId(responseGeneratedId);
		
		itemDAO.save(order.getItems());
		orderDAO.save(order);
		return order;
	}
	
	public Item findAndPopulate(String productId) {
		Properties properties = itemPropertiesDAO.find(productId);
		if (properties == null) {
			return null;
		}
		
		return Item.populateFrom(properties);
	}

	private String getValueFrom(Pattern pattern, String text) {
		Matcher matcher = pattern.matcher(text);
		if (!matcher.find()) {
			throw new IllegalStateException("Could not find pattern in text: " + text);
		}
		
		return matcher.group(1);
	}

	private Order findByCartId(String cartId) {
		return orderDAO.findByCartId(cartId);
	}
	
	private Order findByOrderId(String orderId) {
		return orderDAO.findOne(orderId);
	}

	@Transactional
	public void notifyPayment(String orderId) throws ServiceException {
		Order order = findByOrderId(orderId);
		if (order == null) {
			throw new IllegalArgumentException("Could not find order with id: " + orderId);
		}

		if (isNotBlank(order.getPaid())) {
			LOG.info("Notify for already paid order: {}", orderId);
			return;
		}
		
		LOG.info("Getting status of order: {}", orderId);
		String statusResponse = defaultString(cartService.orderStatus(order.getGeneratedId()));
		LOG.debug("Status response order: {} is: {}", orderId, statusResponse);
		
		String receipt = responseParser.getReceipt(statusResponse);
		if (isBlank(receipt)) {
			throw new IllegalStateException("Order: " + orderId + " is not paid");
		}
		
		LOG.info("Getting cart details of order: {}", orderId);
		String orderQuery = cartService.orderQuery(order.getGeneratedId());
		LOG.debug("Order details for id: {} are: {}", orderId, orderQuery);
		OrderDetails orderDetails = responseParser.getPaidOrderDetails(orderQuery);
		
		order.setPaid(receipt, orderDetails);
		orderDAO.save(order);
		LOG.info("Saved order: {} as paid", orderId);
	}

	public Collection<String> getAllowedFields(String productId) {
		Set<String> allowedFields = new HashSet<String>();
		Properties properties = itemPropertiesDAO.find(productId);
		if (properties == null) {
			return Collections.emptySet();
		}
		
		String rawFields = properties.getProperty("fields");
		if (isBlank(rawFields)) {
			return Collections.emptySet();
		}
		
		for (String field : rawFields.split(",")) {
			if (isNotBlank(field)) {
				allowedFields.add(field.trim());
			}
		}
		
		return allowedFields;
	}
	
	
}
