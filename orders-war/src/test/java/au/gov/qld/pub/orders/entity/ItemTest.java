package au.gov.qld.pub.orders.entity;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import au.gov.qld.pub.orders.service.NotifyType;


public class ItemTest {
    @Test
    public void throwExceptionWhenNotifyCustomerEmailFieldInvalid() {
        assertThrows(IllegalStateException.class, () -> {
            new ItemBuilder().withNotifyCustomerEmailField("invalid").build();
        });
    }

    @Test
    public void dontThrowExceptionWhenNotifyCustomerEmailFieldValid() {
        new ItemBuilder().withNotifyCustomerEmailField("deliveryDetails").build();
    }

    @Test
    public void setPaidIfMoreThan1Quantity() {
        Item item = new ItemBuilder().build();
        item.setQuantityPaid("2");
        assertThat(item.isPaid(), is(true));
        assertThat(item.getQuantityPaid(), is("2"));
    }

    @Test
    public void dontSetPaidWhenNoQuantiy() {
        Item item = new ItemBuilder().build();
        item.setQuantityPaid(null);
        assertThat(item.isPaid(), is(false));
        assertThat(item.getQuantityPaid(), is("0"));

        item.setQuantityPaid(" ");
        assertThat(item.isPaid(), is(false));
        assertThat(item.getQuantityPaid(), is("0"));
    }

    @Test
    public void returnNotifyUriForType() {
    	Item item = new ItemBuilder().withNotifyCustomerFormUri("customer").withNotifyBusinessFormUri("business").build();
    	assertThat(item.getNotifyFormUri(NotifyType.CUSTOMER), is("customer"));
    	assertThat(item.getNotifyFormUri(NotifyType.BUSINESS), is("business"));
    }

    @Test
    public void upgradingCartState() {
        Item item = new ItemBuilder().build();
        item.setCartState(CartState.NEW);
        item.setCartState(CartState.ADDED);
        item.setCartState(CartState.PAID);
    }

    @Test
    public void throwExceptionWhenDowngradingCartStateFromAdded() {
        assertThrows(IllegalStateException.class, () -> {
            Item item = new ItemBuilder().build();
            item.setCartState(CartState.ADDED);
            item.setCartState(CartState.NEW);
        });
    }

    @Test
    public void throwExceptionWhenDowngradingCartStateFromPaid() {
        assertThrows(IllegalStateException.class, () -> {
            Item item = new ItemBuilder().build();
            item.setCartState(CartState.PAID);
            item.setCartState(CartState.ADDED);
        });
    }
}
