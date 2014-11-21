package au.gov.qld.pub.orders.service.ws;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import au.gov.qld.pub.orders.ApplicationContextAwareTest;
import au.gov.qld.pub.orders.dao.ItemPropertiesDAO;
import au.gov.qld.pub.orders.entity.CartState;
import au.gov.qld.pub.orders.entity.Item;
import au.gov.qld.pub.orders.entity.Order;
import au.gov.qld.pub.orders.service.ServiceException;

import com.google.common.collect.ImmutableMap;


public class RequestBuilderIntegrationTest extends ApplicationContextAwareTest {
	static final String CART_ID = "some cart id";
	
	@Autowired RequestBuilder builder;
	@Autowired ItemPropertiesDAO itemPropertiesDAO;
	
	Order order;
	Item item;
	
	@Before
	public void setUp() {
		order = new Order(CART_ID);
		item = Item.populateFrom(itemPropertiesDAO.find("test"));
		item.setFields(ImmutableMap.of("field1", "value1", "field2", "value2"));
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
	}
	
	@Test
	public void createAddRequestWithoutItemsNotNew() throws ServiceException {
		Item paidItem = Item.populateFrom(itemPropertiesDAO.find("test"));
		paidItem.setCartState(CartState.PAID);
		paidItem.setFields(ImmutableMap.of("field1", "value1", "field2", "value2"));
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
}

