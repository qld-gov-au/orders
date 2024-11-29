package au.gov.qld.pub.orders.web;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.repeat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;

import au.gov.qld.pub.orders.web.model.ItemCommand;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.google.common.collect.ImmutableMap;

import au.gov.qld.pub.orders.entity.Item;
import au.gov.qld.pub.orders.entity.Order;
import au.gov.qld.pub.orders.service.ConfigurationService;
import au.gov.qld.pub.orders.service.OrderService;
import au.gov.qld.pub.orders.service.PreCartValidator;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class OrderControllerTest {
    static final String PRODUCT_ID = "product id";
    static final String FULL_URL = "service full url";
    static final String COOKIE_CART_ID = "some cookie cart id";
    static final String REQ_CART_ID = "some request cart id";
    private static final String ERROR_REDIRECT = "some error redirect";
	private static final String PRODUCT_GROUP = "some group";

    @Mock OrderService orderService;
    @Mock HttpServletResponse response;
    @Mock Order order;
    @Mock
    ItemCommand command;
    @Mock Item item;
    @Mock ConfigurationService configurationService;
    @Mock PreCartValidator preCartValidator;

    MockHttpServletRequest request = new MockHttpServletRequest();
    OrderController controller;

    @BeforeEach
    public void setUp() {
        when(command.getProductId()).thenReturn(asList(PRODUCT_ID));
        when(orderService.findAndPopulate(PRODUCT_ID)).thenReturn(item);
        when(configurationService.getErrorRedirect()).thenReturn(ERROR_REDIRECT);
        when(configurationService.getServiceFullUrl()).thenReturn(FULL_URL);
        when(orderService.getAllowedFields(PRODUCT_ID)).thenReturn(asList("allowedfield"));
        when(item.getProductId()).thenReturn(PRODUCT_ID);
        when(item.getProductGroup()).thenReturn(PRODUCT_GROUP);
        controller = new OrderController(orderService, configurationService, asList(preCartValidator));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void confirmAndReturnCatorgisedViewWithNotEmptyFields() {
        request.setParameters(ImmutableMap.of("field", "value", "blank", "", "spaced", " "));
        ModelAndView mav = controller.confirm("category", request);
        assertThat(mav.getViewName(), is("confirm.category"));

        Map<String, String> requestFields = (Map<String, String>)mav.getModel().get("fields");
        assertThat(requestFields.size(), is(1));
        assertThat(requestFields, hasEntry("field", "value"));
    }

    @Test
    public void redirectToErrorOnAddWithInvalidItem() throws Exception {
        when(command.getProductId()).thenReturn(null);
        RedirectView result = controller.add(null, null, command, request, response);
        assertThat(result.getUrl(), is(ERROR_REDIRECT));
        verifyNoInteractions(orderService);
    }

    @SuppressWarnings("unchecked")
	@Test
    public void addWithCartIdFromCookie() throws Exception {
        when(orderService.add(asList(item), COOKIE_CART_ID)).thenReturn(order);
        when(order.getCartId()).thenReturn(COOKIE_CART_ID);

        RedirectView result = controller.add(COOKIE_CART_ID, REQ_CART_ID, command, request, response);
        assertThat(result.getUrl(), is(FULL_URL + "/added"));
        verify(orderService).add(asList(item), COOKIE_CART_ID);
        verify(response).addCookie(argThat(WebUtilsTest.cookieWith(Constants.CART_ID, COOKIE_CART_ID, false)));
        verify(preCartValidator).validate(anyString(), anyString(), anyMap());
    }

    @SuppressWarnings("unchecked")
	@Test
    public void redirectToErrorWhenNoOrderReturnedFromAddCausedByMissingFields() throws Exception {
    	item.setFieldsFromMap(Collections.emptyMap());
        when(orderService.add(asList(item), COOKIE_CART_ID)).thenReturn(null);

        RedirectView result = controller.add(COOKIE_CART_ID, REQ_CART_ID, command, request, response);
        assertThat(result.getUrl(), is(ERROR_REDIRECT));
        verify(orderService).add(asList(item), COOKIE_CART_ID);
        verifyNoInteractions(response);
        verify(preCartValidator).validate(anyString(), anyString(), anyMap());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void addWithCartIdFromRequestWhenCookieNull() throws Exception {
        request.setParameters(ImmutableMap.of("badfield", "badvalue", "allowedfield", "allowedvalue"));
        when(orderService.add(asList(item), REQ_CART_ID)).thenReturn(order);
        when(order.getCartId()).thenReturn(REQ_CART_ID);

        RedirectView result = controller.add(null, REQ_CART_ID, command, request, response);
        assertThat(result.getUrl(), is(FULL_URL + "/added"));
        verify(orderService).add(asList(item), REQ_CART_ID);
        verify(response).addCookie(argThat(WebUtilsTest.cookieWith(Constants.CART_ID, REQ_CART_ID, false)));
        verify(item).setFieldsFromMap((Map<String, String>) argThat(allOf(hasEntry("allowedfield", "allowedvalue"), not(hasEntry("badfield", "badvalue")))));
        verify(preCartValidator).validate(eq(PRODUCT_GROUP), eq(PRODUCT_ID), (Map<String, String>) argThat(allOf(hasEntry("allowedfield", "allowedvalue"), not(hasEntry("badfield", "badvalue")))));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void addWithTruncatedFieldsAndSecuredWhenUrlIsSecure() throws Exception {
    	when(configurationService.getServiceFullUrl()).thenReturn("https://" + FULL_URL);
        request.setParameters(ImmutableMap.of("allowedfield", repeat("a", OrderController.MAX_FIELD_LENGTH + 1)));
        when(orderService.add(asList(item), REQ_CART_ID)).thenReturn(order);
        when(order.getCartId()).thenReturn(REQ_CART_ID);

        RedirectView result = controller.add(null, REQ_CART_ID, command, request, response);
        assertThat(result.getUrl(), is("https://" + FULL_URL + "/added"));
        verify(orderService).add(asList(item), REQ_CART_ID);
        verify(response).addCookie(argThat(WebUtilsTest.cookieWith(Constants.CART_ID, REQ_CART_ID, true)));
        verify(item).setFieldsFromMap((Map<String, String>) argThat(allOf(hasEntry("allowedfield", repeat("a", OrderController.MAX_FIELD_LENGTH)))));
    }

}
