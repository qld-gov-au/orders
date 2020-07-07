package au.gov.qld.pub.orders.service;

import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import au.gov.qld.pub.orders.dao.ItemDAO;
import au.gov.qld.pub.orders.dao.OrderDAO;
import au.gov.qld.pub.orders.entity.CartState;
import au.gov.qld.pub.orders.entity.Item;
import au.gov.qld.pub.orders.entity.Order;
import au.gov.qld.pub.orders.service.ws.CartResponseParser;
import au.gov.qld.pub.orders.service.ws.CartService;
import au.gov.qld.pub.orders.service.ws.OrderDetails;
import au.gov.qld.pub.orders.service.ws.RequestBuilder;

@Service
public class OrderService {
    private static final int ADD_TO_CART_RETRY_DELAY_SECONDS = 3;
	private static final int ADD_TO_CART_RETRY_LIMIT = 3;
	private static final Logger LOG = LoggerFactory.getLogger(OrderService.class);
    private static final Pattern CART_ID_PATTERN = Pattern.compile("<.*[:]?cartId>(.+)</.*[:]?cartId>");
    private static final Pattern GENERATED_ORDER_ID_PATTERN = Pattern.compile("<.*[:]?generatedOrderId>(.+)</.*[:]?generatedOrderId>");
    
    private final CartService cartService;
    private final NotifyService notifyService;
    private final OrderDAO orderDAO;
    private final ItemPropertiesService itemPropertiesService;
    private final ItemDAO itemDAO;
    private final RequestBuilder requestBuilder;
    private final CartResponseParser responseParser;

    @Autowired
    public OrderService(CartService cartService, OrderDAO orderDAO, ItemDAO itemDAO, ItemPropertiesService itemPropertiesService,
            RequestBuilder requestBuilder, CartResponseParser responseParser, NotifyService notifyService) {
        this.cartService = cartService;
        this.orderDAO = orderDAO;
        this.itemDAO = itemDAO;
        this.itemPropertiesService = itemPropertiesService;
        this.requestBuilder = requestBuilder;
        this.responseParser = responseParser;
        this.notifyService = notifyService;
    }
    
    @Transactional(noRollbackFor = ServiceException.class)
    public Order add(List<Item> items, String cartId) throws ServiceException, InterruptedException, IllegalArgumentException {
        Order order = findByCartId(cartId);
        if (order == null) {
            LOG.info("Creating new order for cartId: {}", cartId);
            order = new Order(cartId);
        } else if (isNotBlank(order.getPaid())) {
            LOG.info("Returning customer, creating new order for paid cart id {} which will get a new cartId", cartId);
            order = new Order(null);
        }
        
        for (Item item : items) {
        	if (item.getFieldsMap().isEmpty()) {
        		throw new IllegalArgumentException("Item missing fields");
        	}
            order.add(item);
        }
        
        itemDAO.saveAll(order.getItems());
        orderDAO.save(order);
        
        LOG.info("Sending cart add request for order: {} with cartId: {}", order.getId(), order.getCartId());
        String response = "";
        String responseCartId = "";
        for (int i=0; i < ADD_TO_CART_RETRY_LIMIT; i++) {
	        try {
	        	response = cartService.addToCart(requestBuilder.addRequest(order));
	        	responseCartId = getValueFrom(CART_ID_PATTERN, response);
	        	break;
	        } catch (Exception e) {
	        	LOG.error(e.getMessage(), e);
	        	LOG.warn("Failed to add to cart. Resetting cart ID to null to force a new cart on order: {} which previously had cartId: {}",
	        			order.getId(), order.getCartId());
	        	order.setCartId(null);
				TimeUnit.SECONDS.sleep(ADD_TO_CART_RETRY_DELAY_SECONDS);
	        }
        }
        
        if (isBlank(response) || isBlank(responseCartId)) {
        	throw new ServiceException("Could not add to cart after multiple attempts for order: " + order.getId());
        }
        
        LOG.debug("Cart add response: {}", response);
        for (Item item : order.getItems()) {
            item.setCartState(CartState.ADDED);
        }
        
        String responseGeneratedId = getValueFrom(GENERATED_ORDER_ID_PATTERN, response);
        order.setCartId(responseCartId);
        order.setGeneratedId(responseGeneratedId);
        
        itemDAO.saveAll(order.getItems());
        orderDAO.save(order);
        return order;
    }
    
    public Item findAndPopulate(String productId) {
        ItemPropertiesDTO properties = itemPropertiesService.find(productId);
        if (properties == null) {
            return null;
        }
        
        return Item.createItem(properties);
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
        return orderDAO.findById(orderId).orElse(null);
    }

    @Transactional(rollbackFor = ServiceException.class)
    public boolean notifyPayment(String orderId) throws ServiceException {
        Order order = findByOrderId(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Could not find order with id: " + orderId);
        }

        if (isNotBlank(order.getPaid())) {
            LOG.info("Notify for already paid order: {}", orderId);
            return false;
        }
        
        LOG.info("Getting status of order: {}", orderId);
        String statusResponse = defaultString(cartService.orderStatus(order.getGeneratedId()));
        LOG.debug("Status response order: {} is: {}", orderId, statusResponse);
        
        String receipt = responseParser.getReceipt(statusResponse);
        if (isBlank(receipt)) {
            LOG.info("Order: {} is not paid", orderId);
            return false;
        }
        
        LOG.info("Getting cart details of order: {}", orderId);
        String orderQuery = cartService.orderQuery(order.getGeneratedId());
        LOG.debug("Order details for id: {} are: {}", orderId, orderQuery);
        OrderDetails orderDetails = responseParser.getPaidOrderDetails(orderQuery);
        
        order.setPaid(receipt, orderDetails);
        orderDAO.save(order);
        LOG.info("Saved order: {} as paid", orderId);
        notifyService.send(order);
        return true;
    }

    public Collection<String> getAllowedFields(String productId) {
        Set<String> allowedFields = new HashSet<String>();
        ItemPropertiesDTO properties = itemPropertiesService.find(productId);
        if (properties == null) {
            return Collections.emptySet();
        }
        
        String rawFields = properties.getFields();
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

    public Iterable<String> findUnpaidOrderIds(Date minCreated) {
        return orderDAO.findUnpaidOrdersCreatedAfter(minCreated);
    }

    public void deleteOlderThan(LocalDateTime maxCreated, boolean paid) {
        LOG.info("Cleanup {} orders created on or before {}", (paid ? "paid" : "unpaid"), maxCreated);
        Iterable<Order> toDeletes = paid ? orderDAO.findOlderThanCreatedAndPaid(maxCreated.toDate()) 
                : orderDAO.findOlderThanCreatedAndNotPaid(maxCreated.toDate());
        
        for (Order toDelete : toDeletes) {
            LOG.info("Deleting items and order: {} created at: {}", toDelete.getId(), toDelete.getCreated());
            itemDAO.deleteAll(toDelete.getItems());
            toDelete.getItems().clear();
            orderDAO.deleteById(toDelete.getId());
        }
    }
    
    
}
