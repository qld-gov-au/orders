package au.gov.qld.pub.orders.entity;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import au.gov.qld.pub.orders.service.ws.OrderDetails;

import com.google.common.collect.ImmutableMap;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class OrderTest {
	Item item1;
    Item item2;
    Item item3;
    @Mock OrderDetails orderDetails;
    Map<String, String> deliveryDetails = ImmutableMap.of("delivery", "delivery details");
    Map<String, String> customerDetails = ImmutableMap.of("customer", "customer details");
    private Order order;

    @BeforeEach
    public void setUp() {
        when(orderDetails.getDeliveryDetails()).thenReturn(deliveryDetails);
        when(orderDetails.getCustomerDetails()).thenReturn(customerDetails);
        item1 = new Item();
        item2 = new Item();
        item3 = new Item();

        order = new Order("anything");
        order.add(item1);
        order.add(item2);
        order.add(item3);
    }

    @Test
    public void throwExceptionWhenNoReceiptAndSettingPaid() {
        assertThrows(IllegalStateException.class, () -> {
            order.setPaid(null, null);
        });
    }

    @Test
    public void returnPaidItems() {
    	item1.setQuantityPaid("1");

    	assertThat(order.getItems().size(), greaterThan(1));
    	List<Item> paid = order.getPaidItems();
    	assertThat(paid.size(), is(1));
    	assertThat(paid, hasItem(item1));
    }

    @Test
    public void setPaidAndDetails() {
        when(orderDetails.getOrderlineQuantities()).thenReturn(ImmutableMap.of(item1.getId(), "1", item3.getId(), "2"));

        order.setPaid("receipt", orderDetails);
        assertThat(order.getReceipt(), is("receipt"));
        assertThat(order.getPaid(), notNullValue());

        assertThat(order.getCustomerDetailsMap(), is(customerDetails));
        assertThat(order.getDeliveryDetailsMap(), is(deliveryDetails));
        assertThat(item1.isPaid(), is(true));
        assertThat(item2.isPaid(), is(false));
        assertThat(item3.isPaid(), is(true));
    }

    @Test
    public void returnBundledAndUnbundledItems() {
    	item1.setQuantityPaid("1");
    	item3.setQuantityPaid("1");
    	item1.setBundledDownload(true);
    	item2.setBundledDownload(true);
    	item3.setBundledDownload(false);

    	assertThat(order.getBundledPaidItems().size(), is(1));
    	assertThat(order.getBundledPaidItems(), hasItem(item1));
    	assertThat(order.getUnbundledPaidItems().size(), is(1));
    	assertThat(order.getUnbundledPaidItems(), hasItem(item3));
    }
}

