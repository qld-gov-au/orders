package au.gov.qld.pub.orders.service;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.hamcrest.Matcher;
import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.ImmutableMap;

import au.gov.qld.pub.orders.ApplicationContextAwareTest;
import au.gov.qld.pub.orders.dao.ItemDAO;
import au.gov.qld.pub.orders.dao.OrderDAO;
import au.gov.qld.pub.orders.entity.CartState;
import au.gov.qld.pub.orders.entity.Item;
import au.gov.qld.pub.orders.entity.Order;

import java.util.Optional;

public class OrderServiceIntegrationTest extends ApplicationContextAwareTest {
    @Autowired OrderService service;
    @Autowired OrderDAO orderDAO;
    @Autowired ItemDAO itemDAO;
    
    @Test
    public void addToNewCart() throws Exception {
        Item item = createItem();
        item.setFields(ImmutableMap.of("field1", "value1", "field2", "value2"));
        
        Order order = service.add(asList(item), null);
        assertThat(order.getCartId(), not(nullValue()));
        assertThat(order.getGeneratedId(), not(nullValue()));
        
        Order saved = orderDAO.findById(order.getId()).get();
        assertThat(saved.getCartId(), not(nullValue()));
        assertThat(saved.getGeneratedId(), not(nullValue()));
        
        Matcher<Item> itemWith = allOf(hasProperty("productId", is("test")), hasProperty("cartState", is(CartState.ADDED)));
        assertThat(saved.getItems(), hasItem(itemWith));
    }
    
    @Test
    public void addToNewCartWhenCartIdNotValidFromPAPI() throws Exception {
        Item item = createItem();
        item.setFields(ImmutableMap.of("field1", "value1", "field2", "value2"));
        
        Order order = service.add(asList(item), "bogus");
        assertThat(order.getCartId(), not(nullValue()));
        assertThat(order.getCartId(), not(is("bogus")));
        assertThat(order.getGeneratedId(), not(nullValue()));
        
        Order saved = orderDAO.findById(order.getId()).get();
        assertThat(saved.getCartId(), not(nullValue()));
        assertThat(saved.getGeneratedId(), not(nullValue()));
        
        Matcher<Item> itemWith = allOf(hasProperty("productId", is("test")), hasProperty("cartState", is(CartState.ADDED)));
        assertThat(saved.getItems(), hasItem(itemWith));
    }
    
    @Test
    public void populateItemFromDto() {
        Item item = service.findAndPopulate("test");
        assertThat(item.getProductId(), is("test"));
        assertThat(item.getProductGroup(), is("testgroup"));
        assertThat(item.isBundledDownload(), is(true));
    }

    private Item createItem() {
        return service.findAndPopulate("test");
    }
    
    @Test
    public void deletePaidOrdersOlderThanMaxCreatedDate() {
        LocalDateTime maxCreatedAt = new LocalDateTime().minusDays(10);
        
        Order oldPaid = createOrder(maxCreatedAt, true, createItem());
        Order oldUnpaid = createOrder(maxCreatedAt, false, createItem());
        Order youngPaid = createOrder(maxCreatedAt.plusDays(1), true, createItem());
        Order youngUnpaid = createOrder(maxCreatedAt.plusDays(1), false, createItem());
        
        service.deleteOlderThan(maxCreatedAt, true);
        assertThat(orderDAO.findById(oldPaid.getId()), is(Optional.empty()));
        assertThat(orderDAO.findById(oldUnpaid.getId()).get(), is(oldUnpaid));
        assertThat(orderDAO.findById(youngPaid.getId()).get(), is(youngPaid));
        assertThat(orderDAO.findById(youngUnpaid.getId()).get(), is(youngUnpaid));
        
        service.deleteOlderThan(maxCreatedAt, false);
        assertThat(orderDAO.findById(oldUnpaid.getId()), is(Optional.empty()));
        assertThat(orderDAO.findById(youngPaid.getId()).get(), is(youngPaid));
        assertThat(orderDAO.findById(youngUnpaid.getId()).get(), is(youngUnpaid));
    }
    
    private Order createOrder(LocalDateTime created, boolean paid, Item item) {
        item = itemDAO.save(item);
        Order order = new Order("");
        order.setCreatedAt(created.toDate());
        order.add(item);
        order.setPaid(paid ? "paid" : null);
        orderDAO.save(order);
        return order;
    }
}
