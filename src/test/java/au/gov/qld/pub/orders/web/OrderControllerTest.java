package au.gov.qld.pub.orders.web;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.repeat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import au.gov.qld.pub.orders.entity.Item;
import au.gov.qld.pub.orders.entity.Order;
import au.gov.qld.pub.orders.service.ConfigurationService;
import au.gov.qld.pub.orders.service.OrderService;
import au.gov.qld.pub.orders.service.ServiceException;

import com.google.common.collect.ImmutableMap;

@RunWith(MockitoJUnitRunner.class)
public class OrderControllerTest {
	static final String PRODUCT_ID = "product id";
	static final String FULL_URL = "service full url";
	static final String COOKIE_CART_ID = "some cookie cart id";
	static final String REQ_CART_ID = "some request cart id";
	private static final String ERROR_REDIRECT = "some error redirect";
	
	@Mock OrderService orderService;
	@Mock HttpServletResponse response;
	@Mock Order order;
	@Mock ItemCommand command;
	@Mock Item item;
	@Mock ConfigurationService configurationService;
	
	MockHttpServletRequest request = new MockHttpServletRequest();
	OrderController controller;

	@Before
	public void setUp() {
		when(command.getProductId()).thenReturn(asList(PRODUCT_ID));
		when(orderService.findAndPopulate(PRODUCT_ID)).thenReturn(item);
		when(configurationService.getErrorRedirect()).thenReturn(ERROR_REDIRECT);
		when(configurationService.getServiceFullUrl()).thenReturn(FULL_URL);
		when(orderService.getAllowedFields(PRODUCT_ID)).thenReturn(asList("allowedfield"));
		when(item.getProductId()).thenReturn(PRODUCT_ID);
		controller = new OrderController(orderService, configurationService);
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
	public void redirectToErrorOnAddWithInvalidItem() throws ServiceException {
		when(command.getProductId()).thenReturn(null);
		RedirectView result = controller.add(null, null, command, request, response);
		assertThat(result.getUrl(), is(ERROR_REDIRECT));
		verifyZeroInteractions(orderService);
	}
	
	@Test
	public void addWithCartIdFromCookie() throws ServiceException {
		when(orderService.add(asList(item), COOKIE_CART_ID)).thenReturn(order);
		when(order.getCartId()).thenReturn(COOKIE_CART_ID);
		
		RedirectView result = controller.add(COOKIE_CART_ID, REQ_CART_ID, command, request, response);
		assertThat(result.getUrl(), is(FULL_URL + "/added"));
		verify(orderService).add(asList(item), COOKIE_CART_ID);
		verify(response).addCookie(argThat(WebUtilsTest.cookieWith(Constants.CART_ID, COOKIE_CART_ID)));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void addWithCartIdFromRequestWhenCookieNull() throws ServiceException {
		request.setParameters(ImmutableMap.of("badfield", "badvalue", "allowedfield", "allowedvalue"));
		when(orderService.add(asList(item), REQ_CART_ID)).thenReturn(order);
		when(order.getCartId()).thenReturn(REQ_CART_ID);
		
		RedirectView result = controller.add(null, REQ_CART_ID, command, request, response);
		assertThat(result.getUrl(), is(FULL_URL + "/added"));
		verify(orderService).add(asList(item), REQ_CART_ID);
		verify(response).addCookie(argThat(WebUtilsTest.cookieWith(Constants.CART_ID, REQ_CART_ID)));
		verify(item).setFields((Map<String, String>) argThat(allOf(hasEntry("allowedfield", "allowedvalue"), not(hasEntry("badfield", "badvalue")))));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void addWithTruncatedFields() throws ServiceException {
		request.setParameters(ImmutableMap.of("allowedfield", repeat("a", 201)));
		when(orderService.add(asList(item), REQ_CART_ID)).thenReturn(order);
		when(order.getCartId()).thenReturn(REQ_CART_ID);
		
		RedirectView result = controller.add(null, REQ_CART_ID, command, request, response);
		assertThat(result.getUrl(), is(FULL_URL + "/added"));
		verify(orderService).add(asList(item), REQ_CART_ID);
		verify(response).addCookie(argThat(WebUtilsTest.cookieWith(Constants.CART_ID, REQ_CART_ID)));
		verify(item).setFields((Map<String, String>) argThat(allOf(hasEntry("allowedfield", repeat("a", 200)))));
	}
	
}
