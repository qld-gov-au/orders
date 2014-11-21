package au.gov.qld.bdm.orders.service;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.StringWriter;
import java.io.Writer;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

import au.gov.qld.bdm.orders.dao.OrderDAO;
import au.gov.qld.bdm.orders.entity.Item;
import au.gov.qld.bdm.orders.entity.Order;

import com.google.common.collect.ImmutableMap;

import freemarker.template.Configuration;
import freemarker.template.Template;

@RunWith(MockitoJUnitRunner.class)
public class NotifyServiceTest {
	private static final String CUSTOMER_SUBJECT = "customer email subject";
	private static final String BUSINESS_SUBJECT = "business email subject";
	private static final String PRODUCT_ID = "test";
	private static final String BUSINESS_BODY = "business template";
	private static final String CUSTOMER_BODY = "customer template";
	private static final String CUSTOMER_TO = "some to address";
	private static final String BUSINESS_TO = "business to email";
	private static final String FROM = "some from address";
	private static final String TEMPLATED = "some subject templated";
	
	@Mock ConfigurationService configurationService;
	@Mock OrderDAO orderDAO;
	@Mock MailSender mailSender;
	@Mock OrderGrouper orderGrouper;
	@Mock Order order;
	@Mock Order groupedOrder;
	@Mock Item item;
	@Mock Configuration configuration;
	@Mock Template customerTemplate;
	@Mock Template businessTemplate;
	@Mock InlineTemplateService inlineTemplateService;
	
	NotifyService service;

	@SuppressWarnings("rawtypes")
	@Before
	public void setUp() throws Exception {
		when(orderGrouper.byProductGroup(order)).thenReturn(ImmutableMap.of(PRODUCT_ID, groupedOrder));
		when(groupedOrder.getItems()).thenReturn(asList(item));
		doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				StringWriter writer = (StringWriter)invocation.getArguments()[1];
				writer.write(CUSTOMER_BODY);
				return null;
			}			
		}).when(customerTemplate).process(anyMap(), isA(Writer.class));
		doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				StringWriter writer = (StringWriter)invocation.getArguments()[1];
				writer.write(BUSINESS_BODY);
				return null;
			}			
		}).when(businessTemplate).process(anyMap(), isA(Writer.class));
		
		when(inlineTemplateService.template("subject", BUSINESS_SUBJECT, groupedOrder)).thenReturn(TEMPLATED);
		when(inlineTemplateService.template("subject", CUSTOMER_SUBJECT, groupedOrder)).thenReturn(TEMPLATED);
		when(configurationService.getMailFrom()).thenReturn(FROM);
		when(configuration.getTemplate(PRODUCT_ID + ".customer.email.ftl")).thenReturn(customerTemplate);
		when(configuration.getTemplate(PRODUCT_ID + ".business.email.ftl")).thenReturn(businessTemplate);
		when(orderDAO.findOne(order.getId())).thenReturn(order);
		service = new NotifyService(configurationService, orderDAO, mailSender, orderGrouper, inlineTemplateService) {
			@Override
			protected Configuration getTemplateConfiguration() {
				return configuration;
			}
		};
	}
	
	@Test
	public void dontNotifyAlreadyNotified() throws ServiceException {
		when(order.getNotified()).thenReturn("anything");
		service.send(order.getId());

		verifyZeroInteractions(mailSender);
		verify(order, never()).setNotified(anyString());
		verify(orderDAO, never()).save(order);
	}
	
	@Test
	public void notifyToBusiness() throws ServiceException {
		when(item.getNotifyBusinessEmail()).thenReturn(BUSINESS_TO);
		when(item.getNotifyBusinessEmailSubject()).thenReturn(BUSINESS_SUBJECT);
		service.send(order.getId());

		verify(mailSender).send(argThat(messageWith(BUSINESS_TO, FROM, TEMPLATED, BUSINESS_BODY)));
		verify(order).setNotified(anyString());
		verify(orderDAO).save(order);
	}

	@Test
	public void notifyToCustomer() throws ServiceException {
		when(item.getNotifyCustomerEmailField()).thenReturn("customerDetails");
		when(item.getNotifyCustomerEmailSubject()).thenReturn(CUSTOMER_SUBJECT);
		when(groupedOrder.getCustomerDetailsMap()).thenReturn(ImmutableMap.of("email", CUSTOMER_TO));
		service.send(order.getId());

		verify(mailSender).send(argThat(messageWith(CUSTOMER_TO, FROM, TEMPLATED, CUSTOMER_BODY)));
		verify(order).setNotified(anyString());
		verify(orderDAO).save(order);
	}
	
	private Matcher<SimpleMailMessage> messageWith(final String to, final String from, final String subject, final String body) {
		return new BaseMatcher<SimpleMailMessage>() {
			@Override
			public boolean matches(Object arg0) {
				SimpleMailMessage message = (SimpleMailMessage)arg0;
				return to.equals(message.getTo()[0]) && from.equals(message.getFrom()) &&
						subject.equals(message.getSubject()) && body.equals(message.getText());
			}

			@Override
			public void describeTo(Description arg0) {
			}
		};
	}
}
