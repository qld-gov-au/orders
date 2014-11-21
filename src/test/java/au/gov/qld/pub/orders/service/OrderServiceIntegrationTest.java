package au.gov.qld.bdm.orders.service;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import au.gov.qld.bdm.orders.ApplicationContextAwareTest;
import au.gov.qld.bdm.orders.dao.OrderDAO;
import au.gov.qld.bdm.orders.entity.CartState;
import au.gov.qld.bdm.orders.entity.Item;
import au.gov.qld.bdm.orders.entity.Order;
import au.gov.qld.bdm.orders.web.ItemCommand;

import com.google.common.collect.ImmutableMap;

public class OrderServiceIntegrationTest extends ApplicationContextAwareTest {
	@Autowired OrderService service;
	@Autowired OrderDAO orderDAO;
	
	@Test
	public void addToNewCart() throws ServiceException {
		ItemCommand command = new ItemCommand();
		command.setProductId(asList("test"));
		Item item = service.findAndPopulate("test");
		item.setFields(ImmutableMap.of("field1", "value1", "field2", "value2"));
		
		Order order = service.add(asList(item), null);
		assertThat(order.getCartId(), not(nullValue()));
		assertThat(order.getGeneratedId(), not(nullValue()));
		
		Order saved = orderDAO.findOne(order.getId());
		assertThat(saved.getCartId(), not(nullValue()));
		assertThat(saved.getGeneratedId(), not(nullValue()));
		
		Matcher<Item> itemWith = allOf(hasProperty("productId", is("test")), hasProperty("cartState", is(CartState.ADDED)));
		assertThat(saved.getItems(), hasItem(itemWith));
	}

	@Test
	public void populateItemFromCommand() {
		ItemCommand command = new ItemCommand();
		command.setProductId(asList("test"));
		Item item = service.findAndPopulate("test");
		assertThat(item.getProductId(), is("test"));
	}
}
