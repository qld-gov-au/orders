package au.gov.qld.pub.orders.service.ws;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import au.gov.qld.pub.orders.ApplicationContextAwareTest;
import au.gov.qld.pub.orders.entity.CartState;
import au.gov.qld.pub.orders.entity.Item;
import au.gov.qld.pub.orders.entity.Order;
import au.gov.qld.pub.orders.service.ConfigurationService;
import au.gov.qld.pub.orders.service.ItemPropertiesService;
import au.gov.qld.pub.orders.service.PaymentInformation;
import au.gov.qld.pub.orders.service.ServiceException;
import au.gov.qld.pub.orders.service.StubPaymentInformationService;

import com.google.common.collect.ImmutableMap;


public class RequestBuilderIT extends ApplicationContextAwareTest {
    static final String CART_ID = "some cart id";
    private static final String SOURCE_URL = "some source";
    private static final String NTP_ID = "some ntp id";

    @Autowired RequestBuilder builder;
    @Autowired ItemPropertiesService itemPropertiesService;
    @Autowired ConfigurationService config;

    Order order;
    Item item;

    @BeforeEach
    public void setUp() {
        order = new Order(CART_ID);
        item = Item.createItem(itemPropertiesService.find("test"));
        item.setFieldsFromMap(ImmutableMap.of("field1", "value1", "field2", "value2"));
        order.add(item);
    }

    @Test
    public void createAddRequest() throws ServiceException {
        String request = builder.addRequest(order);
        assertThat(request, containsString(order.getId()));
        assertThat(request, containsString("<cartId>" + CART_ID + "</cartId>"));
        assertThat(request, containsString("<orderline id=\"" + item.getId() + "\">"));
        assertThat(request, containsString("<deliveryAddressRequest type=\"POST\"/>"));
        assertThat(request, containsString("<customerDetailsRequest type=\"EMAIL\" required=\"true\"/>"));
        assertThat(request, containsString("<customerDetailsRequest type=\"PHONE\" required=\"true\"/>"));
        assertThat(request, containsString("title=\"test title value1\""));
        assertThat(request, containsString("description=\"test description value2\""));
        assertThat(request, containsString("accounting costCenter=\"cc\" glCode=\"gl\" taxCode=\"tc\" narrative=\"narrative\""));
    }

    @Test
    public void createAddRequestWithoutItemsNotNew() throws ServiceException {
        Item paidItem = Item.createItem(itemPropertiesService.find("test"));
        paidItem.setCartState(CartState.PAID);
        paidItem.setFieldsFromMap(ImmutableMap.of("field1", "value1", "field2", "value2"));
        order.add(paidItem);

        String request = builder.addRequest(order);
        assertThat(request, containsString(order.getId()));
        assertThat(request, containsString("<cartId>" + CART_ID + "</cartId>"));
        assertThat(request, containsString("<orderline id=\"" + item.getId() + "\">"));
        assertThat(request, containsString("<deliveryAddressRequest type=\"POST\"/>"));
        assertThat(request, containsString("<customerDetailsRequest type=\"EMAIL\" required=\"true\"/>"));
        assertThat(request, containsString("<customerDetailsRequest type=\"PHONE\" required=\"true\"/>"));
        assertThat(request, containsString("title=\"test title value1\""));
        assertThat(request, containsString("description=\"test description value2\""));
        assertThat(request, not(containsString("<orderline id=\"" + paidItem.getId() + "\">")));
    }

    @Test
    public void createAddRequestWithoutCart() throws ServiceException {
        order.setCartId(null);
        String request = builder.addRequest(order);
        assertThat(request, not(containsString("<cartId>")));
    }

    @Test
    public void createNoticeToPayRequestWithInformation() throws ServiceException {
        PaymentInformation paymentInformation = new StubPaymentInformationService().fetch("some id");
        String request = builder.noticeToPay(paymentInformation, NTP_ID, SOURCE_URL);
        assertThat(request, containsString("<onlineService>" + config.getNoticeToPayServiceWsUsername() + "</onlineService>"));
        assertThat(request, containsString("<paymentRequest id=\"" + NTP_ID +  "\">"));
        assertThat(request, containsString("<returnUrl>" + SOURCE_URL + "</returnUrl>"));
        assertThat(request, containsString("<gst>45</gst>"));
        assertThat(request, containsString("<cost>123</cost>"));
        assertThat(request, containsString("<notificationUrl>" + config.getNoticeToPayServiceWsNotify() + "/ntp-notify/" + NTP_ID + "</notificationUrl>"));
    }

    @Test
    public void createNoticeToPayQueryRequest() {
        String request = builder.noticeToPayQuery(NTP_ID);
        assertThat(request, containsString("<paymentRequestId>" + NTP_ID + "</paymentRequestId>"));
    }
}

