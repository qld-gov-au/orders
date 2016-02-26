package au.gov.qld.pub.orders.service;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import au.gov.qld.pub.orders.dao.ItemDAO;
import au.gov.qld.pub.orders.dao.OrderDAO;
import au.gov.qld.pub.orders.entity.Item;
import au.gov.qld.pub.orders.entity.ItemProperties;
import au.gov.qld.pub.orders.entity.Order;
import au.gov.qld.pub.orders.service.ws.CartResponseParser;
import au.gov.qld.pub.orders.service.ws.CartService;
import au.gov.qld.pub.orders.service.ws.OrderDetails;
import au.gov.qld.pub.orders.service.ws.RequestBuilder;
import au.gov.qld.pub.orders.web.ItemCommand;

import com.google.common.collect.ImmutableMap;

@RunWith(MockitoJUnitRunner.class)
public class OrderServiceTest {
    static final String ORDER_STATUS = "some status";
    static final String RECEIPT = "some receipt";
    static final String ORDER_DETAILS = "some order details";
    static final String CART_ID = "some cart id";
    static final String GENERATED_ID = "some generated id";
    static final String PRODUCT_ID = "product id";
    static final String ADD_REQUEST = "some add request";
    
    OrderService orderService;
    Order order;
    
    @Mock CartService cartService;
    @Mock OrderDAO orderDAO;
    @Mock ItemPropertiesService itemPropertiesService;
    @Mock ItemDAO itemDAO;
    @Mock Item item;
    @Mock ItemCommand command;
    @Mock RequestBuilder requestBuilder;
    @Mock ItemProperties properties;
    @Mock CartResponseParser responseParser;
    @Mock OrderDetails orderDetails;
    @Mock NotifyService notifyService;
    Map<String, String> customerDetails;
    Map<String, String> deliveryDetails;
    
    @Before
    public void setUp() throws ServiceException {
        order = new Order(CART_ID);
        deliveryDetails = ImmutableMap.of("delivery type", "value");
        customerDetails = ImmutableMap.of("customer type", "value");

        when(requestBuilder.addRequest(order)).thenReturn(ADD_REQUEST);
        when(orderDAO.findByCartId(CART_ID)).thenReturn(order);
        when(command.getProductId()).thenReturn(asList(PRODUCT_ID));        
        when(cartService.addToCart(ADD_REQUEST)).thenReturn("<cartId>" + CART_ID + "</cartId>"
                + "<generatedOrderId>" + GENERATED_ID + "</generatedOrderId>");
        when(responseParser.getReceipt(ORDER_STATUS)).thenReturn(RECEIPT);
        when(orderDetails.getCustomerDetails()).thenReturn(customerDetails);
        when(orderDetails.getDeliveryDetails()).thenReturn(deliveryDetails);
        when(item.getFieldsMap()).thenReturn(ImmutableMap.of("field", "value"));
        
        
        orderService = new OrderService(cartService, orderDAO, itemDAO, itemPropertiesService, 
                requestBuilder, responseParser, notifyService);
    }
    
    @Test
    public void addToNewOrder() throws ServiceException {
        Matcher<Order> orderWithCartId = allOf(hasProperty("cartId", nullValue()),
                hasProperty("items", is(asList(item))));
        
        when(requestBuilder.addRequest(argThat(orderWithCartId))).thenReturn(ADD_REQUEST);
        Order addedOrder = orderService.add(asList(item), null);
        assertThat(addedOrder.getCartId(), is(CART_ID));
        assertThat(addedOrder.getGeneratedId(), is(GENERATED_ID));
        verify(orderDAO, times(2)).save(addedOrder);
        assertThat(addedOrder.getItems(), is(asList(item)));
    }
    
    @SuppressWarnings("unchecked")
	@Test
    public void throwExceptionIfItemMissingFields() throws ServiceException {
    	when(item.getFieldsMap()).thenReturn(Collections.EMPTY_MAP);
        try {
        	orderService.add(asList(item), null);
        	fail("Should have thrown exception");
        } catch (ServiceException e) {
        	assertThat(e.getMessage(), is("Item missing fields"));
        	verify(orderDAO, never()).save(isA(Order.class));
        }
    }
    
    @Test
    public void addToNewOrderWithOptionalNamespaceInResponse() throws ServiceException {
        when(cartService.addToCart(ADD_REQUEST)).thenReturn("<ns1:cartId>" + CART_ID + "</ns1:cartId>"
                + "<ns1:generatedOrderId>" + GENERATED_ID + "</ns1:generatedOrderId>");
        Matcher<Order> orderWithCartId = allOf(hasProperty("cartId", nullValue()),
                hasProperty("items", is(asList(item))));
        
        when(requestBuilder.addRequest(argThat(orderWithCartId))).thenReturn(ADD_REQUEST);
        Order addedOrder = orderService.add(asList(item), null);
        assertThat(addedOrder.getCartId(), is(CART_ID));
        assertThat(addedOrder.getGeneratedId(), is(GENERATED_ID));
        verify(orderDAO, times(2)).save(addedOrder);
        assertThat(addedOrder.getItems(), is(asList(item)));
    }
    
    @Test
    public void createNewOrderWhenCartPaid() throws ServiceException {
        order.setPaid("something", new OrderDetails());
        Matcher<Order> orderWithCartId = allOf(hasProperty("cartId", nullValue()),
                hasProperty("items", is(asList(item))));
        
        when(requestBuilder.addRequest(argThat(orderWithCartId))).thenReturn(ADD_REQUEST);
        Order addedOrder = orderService.add(asList(item), null);
        assertThat(addedOrder.getCartId(), is(CART_ID));
        assertThat(addedOrder.getGeneratedId(), is(GENERATED_ID));
        verify(orderDAO, times(2)).save(addedOrder);
        assertThat(addedOrder.getItems(), is(asList(item)));
    }
    
    @Test
    public void addToExistingOrder() throws ServiceException {
        Matcher<Order> orderWithCartId = allOf(hasProperty("cartId", is(CART_ID)),
                hasProperty("items", is(asList(item))));
        
        when(requestBuilder.addRequest(argThat(orderWithCartId))).thenReturn(ADD_REQUEST);
        Order addedOrder = orderService.add(asList(item), CART_ID);
        assertThat(addedOrder.getCartId(), is(CART_ID));
        assertThat(addedOrder.getGeneratedId(), is(GENERATED_ID));
        verify(orderDAO, times(2)).save(addedOrder);
        assertThat(addedOrder.getItems(), is(asList(item)));
    }
    
    @Test
    public void itemFromDAO() {
        when(itemPropertiesService.find(PRODUCT_ID)).thenReturn(properties);
        assertThat(orderService.findAndPopulate(PRODUCT_ID), notNullValue());
    }
    
    @Test
    public void nullItemWhenProductIdNotFound() {
        when(itemPropertiesService.find(PRODUCT_ID)).thenReturn(null);
        assertThat(orderService.findAndPopulate(PRODUCT_ID), nullValue());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void throwExceptionWhenUnknownOrderOnNotify() throws ServiceException {
        when(orderDAO.findOne(anyString())).thenReturn(null);
        orderService.notifyPayment("anything");
    }

    @Test
    public void doNothingWhenCartNotPaidOnNotify() throws ServiceException {
        String id = order.getId();
        when(orderDAO.findOne(id)).thenReturn(order);
        
        assertThat(orderService.notifyPayment(id), is(false));
        assertThat(order.getPaid(), nullValue());
        verify(orderDAO, never()).save(order);
        verifyZeroInteractions(notifyService);
    }
    
    @Test
    public void doNothingWhenAlreadyPaidOnNotify() throws ServiceException {
        String id = order.getId();
        order.setPaid("receipt", orderDetails);
        when(orderDAO.findOne(id)).thenReturn(order);
        
        assertThat(orderService.notifyPayment(id), is(false));
        verify(orderDAO, never()).save(order);
        verifyZeroInteractions(notifyService);
    }
    
    @Test
    public void saveOrderAsPaidWhenCartPaidOnNotify() throws ServiceException {
        String generatedId = RandomStringUtils.randomAlphabetic(10);
        order.setGeneratedId(generatedId);
        
        when(cartService.orderStatus(generatedId)).thenReturn(ORDER_STATUS);
        when(cartService.orderQuery(generatedId)).thenReturn(ORDER_DETAILS);
        
        String id = order.getId();
        when(responseParser.getPaidOrderDetails(ORDER_DETAILS)).thenReturn(orderDetails);
        when(orderDAO.findOne(id)).thenReturn(order);
        
        assertThat(orderService.notifyPayment(id), is(true));
        assertThat(order.getPaid(), notNullValue());
        assertThat(order.getReceipt(), is(RECEIPT));
        
        assertThat(order.getDeliveryDetailsMap(), is(deliveryDetails));
        assertThat(order.getCustomerDetailsMap(), is(customerDetails));
        verify(orderDAO).save(order);
        verify(notifyService).send(order);
    }
    
    @Test
    public void returnCollectionOfAllowedFieldsFromItemProperties() {
        ItemProperties properties1 = mock(ItemProperties.class);
        when(properties1.getFields()).thenReturn("field1, field2");
        when(itemPropertiesService.find(PRODUCT_ID)).thenReturn(properties1);
        
        assertThat(orderService.getAllowedFields(PRODUCT_ID), hasItems("field1", "field2"));
    }
    
    @Test
    public void returnEmptyCollectionOfAllowedFieldsWhenUnknownItem() {
        when(itemPropertiesService.find(PRODUCT_ID)).thenReturn(null);
        assertThat((Set<String>)orderService.getAllowedFields(PRODUCT_ID), is(Collections.EMPTY_SET));
    }
    
    @Test
    public void returnEmptyCollectionOfAllowedFieldsWhenItemHasNoAllowedFields() {
        ItemProperties properties1 = mock(ItemProperties.class);
        when(properties1.getFields()).thenReturn(null);
        when(itemPropertiesService.find(PRODUCT_ID)).thenReturn(properties1);
        
        assertThat((Set<String>)orderService.getAllowedFields(PRODUCT_ID), is(Collections.EMPTY_SET));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void returnUnpaidOrdersAfterMinCreated() {
        Date minCreated = new Date();
        Iterable<String> unpaidAfterMinCreated = mock(Iterable.class);
        when(orderDAO.findUnpaidOrdersCreatedAfter(minCreated)).thenReturn(unpaidAfterMinCreated);
        assertThat(orderService.findUnpaidOrderIds(minCreated), is(unpaidAfterMinCreated));
    }
}
