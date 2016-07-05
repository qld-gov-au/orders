package au.gov.qld.pub.orders.service;

import static com.google.common.collect.ImmutableMap.of;
import static java.util.Arrays.asList;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import javax.mail.Address;
import javax.mail.Message.RecipientType;
import javax.mail.internet.MimeMessage;

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
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import au.gov.qld.pub.orders.dao.OrderDAO;
import au.gov.qld.pub.orders.entity.Item;
import au.gov.qld.pub.orders.entity.Order;
import freemarker.template.Configuration;
import freemarker.template.Template;

@RunWith(MockitoJUnitRunner.class)
public class NotifyServiceTest {
    private static final String CUSTOMER_SUBJECT = "customer email subject";
    private static final String BUSINESS_SUBJECT = "business email subject";
    private static final String PRODUCT_ID = "test";
    private static final String BUSINESS_BODY = "business template";
    private static final String CUSTOMER_BODY = "customer template";
    private static final String CUSTOMER_TO = "sometoaddress";
    private static final String BUSINESS_TO = "businesstoemail";
    private static final String FROM = "somefromaddress";
    private static final String TEMPLATED = "some subject templated";
    private static final String PAID = "some paid at";
    
    @Mock ConfigurationService configurationService;
    @Mock OrderDAO orderDAO;
    @Mock JavaMailSender mailSender;
    @Mock OrderGrouper orderGrouper;
    @Mock Order order;
    @Mock Order groupedOrder;
    @Mock Item item;
    @Mock Item unpaidItem;
    @Mock Configuration configuration;
    @Mock Template customerTemplate;
    @Mock Template businessTemplate;
    @Mock InlineTemplateService inlineTemplateService;
    @Mock MimeMessage message;
    @Mock AttachmentService attachmentService;
    @Mock InputStream attachmentStream;
    @Mock AdditionalMailContentService additionalMailContentService;
    
    NotifyService service;

    @SuppressWarnings("rawtypes")
    @Before
    public void setUp() throws Exception {
        when(orderGrouper.paidByProductGroup(order)).thenReturn(of(PRODUCT_ID, groupedOrder));
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
        
        when(mailSender.createMimeMessage()).thenReturn(message);
        when(inlineTemplateService.template("subject", BUSINESS_SUBJECT, groupedOrder)).thenReturn(TEMPLATED);
        when(inlineTemplateService.template("subject", CUSTOMER_SUBJECT, groupedOrder)).thenReturn(TEMPLATED);
        when(configurationService.getMailFrom()).thenReturn(FROM);
        when(configuration.getTemplate(PRODUCT_ID + ".customer.email.ftl")).thenReturn(customerTemplate);
        when(configuration.getTemplate(PRODUCT_ID + ".business.email.ftl")).thenReturn(businessTemplate);
        when(orderDAO.findOne(order.getId())).thenReturn(order);
        service = new NotifyService(configurationService, orderDAO, mailSender, orderGrouper, inlineTemplateService, attachmentService, additionalMailContentService) {
            @Override
            protected Configuration getTemplateConfiguration() {
                return configuration;
            }
        };
    }
    
    @Test
    public void dontNotifyAlreadyNotified() throws ServiceException {
        when(order.getNotified()).thenReturn("anything");
        service.send(order);

        verifyZeroInteractions(mailSender);
        verify(order, never()).setNotified(anyString());
        verify(orderDAO, never()).save(order);
        verifyZeroInteractions(additionalMailContentService);
    }
    
    @Test
    public void notifyToBusiness() throws Exception {
        when(item.isPaid()).thenReturn(true);
        when(item.getFieldsMap()).thenReturn((Map<String, String>)of("field", "value"));
		when(order.getPaidItems()).thenReturn(asList(item));
		when(groupedOrder.getPaidItems()).thenReturn(asList(item));

		when(item.getNotifyBusinessFormFilename()).thenReturn("businessFile");
        when(item.getNotifyBusinessEmail()).thenReturn(BUSINESS_TO);
        when(item.getNotifyBusinessEmailSubject()).thenReturn(BUSINESS_SUBJECT);
        when(order.getPaid()).thenReturn(PAID);
        service.send(order);

        verify(mailSender).send(message);
        verify(message).setText(BUSINESS_BODY);
        verify(message).setRecipient(eq(RecipientType.TO), argThat(addressOf(BUSINESS_TO)));
        verify(message).setFrom(argThat(addressOf(FROM)));
        verify(attachmentService).retrieve(groupedOrder, NotifyType.BUSINESS);
        verify(order).setNotified(anyString());
        verify(orderDAO).save(order);
		verify(additionalMailContentService).append(eq(message), isA(MimeMessageHelper.class), eq(false), eq(asList((Map<String, String>)of("field", "value"))));
    }
    
    @Test
    public void dontNotifyWhenNotPaid() throws Exception {
        when(order.getPaid()).thenReturn(null);
        service.send(order);

        verifyZeroInteractions(mailSender);
        verifyZeroInteractions(attachmentService);
        verifyZeroInteractions(additionalMailContentService);
        verify(order, never()).setNotified(anyString());
        verify(orderDAO, never()).save(order);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void notifyToCustomerFromCustomerDetails() throws Exception {
    	when(item.getNotifyCustomerFormFilename()).thenReturn("customerFile");
        when(item.getNotifyCustomerEmailField()).thenReturn("customerDetails");
        when(item.getNotifyCustomerEmailSubject()).thenReturn(CUSTOMER_SUBJECT);
        when(groupedOrder.getCustomerDetailsMap()).thenReturn(of("email", CUSTOMER_TO));
        when(groupedOrder.getPaidItems()).thenReturn(asList(item));
        when(order.getPaid()).thenReturn(PAID);
        service.send(order);

        verify(mailSender).send(message);
        verify(message).setText(CUSTOMER_BODY);
        verify(message).setRecipient(eq(RecipientType.TO), argThat(addressOf(CUSTOMER_TO)));
        verify(message).setFrom(argThat(addressOf(FROM)));
        verify(attachmentService).retrieve(groupedOrder, NotifyType.CUSTOMER);
        verify(order).setNotified(anyString());
        verify(orderDAO).save(order);
        verify(additionalMailContentService).append(eq(message), isA(MimeMessageHelper.class), eq(true), anyList());
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void notifyToCustomerFromDeliveryDetails() throws Exception {
        when(order.getPaid()).thenReturn(PAID);
        when(order.getPaidItems()).thenReturn(asList(item));
        when(groupedOrder.getPaidItems()).thenReturn(asList(item));
        when(item.getNotifyCustomerFormFilename()).thenReturn("customerFile");
        when(item.getNotifyCustomerEmailField()).thenReturn("deliveryDetails");
        when(item.getNotifyCustomerEmailSubject()).thenReturn(CUSTOMER_SUBJECT);
        when(groupedOrder.getDeliveryDetailsMap()).thenReturn(of("email", CUSTOMER_TO));        
        service.send(order);

        verify(mailSender).send(message);
        verify(message).setText(CUSTOMER_BODY);
        verify(message).setRecipient(eq(RecipientType.TO), argThat(addressOf(CUSTOMER_TO)));
        verify(message).setFrom(argThat(addressOf(FROM)));
        verify(attachmentService).retrieve(groupedOrder, NotifyType.CUSTOMER);
        verify(order).setNotified(anyString());
        verify(orderDAO).save(order);
        verify(additionalMailContentService).append(eq(message), isA(MimeMessageHelper.class), eq(true), anyList());
    }
    
    private Matcher<Address> addressOf(final String to) {
        return new BaseMatcher<Address>() {
            @Override
            public boolean matches(Object item) {
                Address address = (Address)item;
                return address.toString().equals(to);
            }

            @Override
            public void describeTo(Description description) {
            }
        };
    }
}
