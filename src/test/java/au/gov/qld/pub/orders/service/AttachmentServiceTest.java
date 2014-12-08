package au.gov.qld.pub.orders.service;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import au.gov.qld.pub.orders.entity.Item;
import au.gov.qld.pub.orders.entity.Order;

import com.google.common.collect.ImmutableMap;

@RunWith(MockitoJUnitRunner.class)
public class AttachmentServiceTest {
    private static final Integer RETRY_COUNT = 1;
    private static final Integer RETRY_WAIT = 1;
    private static final Integer TIMEOUT = 1;
	private static final String PAID_AT = "paid at";
    private static final String RECEIPT = "receipt";
    private static final String QUANTITY_PAID = "1";
    private static final String BUSINESS_CONTENT = "business content";
    private static final String CUSTOMER_CONTENT = "customer content";
    private static final String BUSINESS_FORM_URI = "http://example.com";
    private static final String CUSTOMER_FORM_FILE_NAME = "some customer form file name";
    private static final String CUSTOMER_FORM_URI = "http://example.com";
    private static final String BUSINESS_FORM_FILE_NAME = "some business form file name";
    private static final String ITEM_ID = "some item id";

    AttachmentService service;
    
    @Mock Order order;
    @Mock Item item;
    @Mock HttpClient client;
    @Mock CloseableHttpResponse businessResponse;
    @Mock CloseableHttpResponse customerResponse;
    @Mock StatusLine statusLine;
    @Mock ConfigurationService config;
    
    Map<String, String> fieldsMap = ImmutableMap.of("name", "value");
    
    @Before
    public void setUp() throws Exception {
        when(order.getItems()).thenReturn(asList(item));
        when(order.getPaid()).thenReturn(PAID_AT);
        when(order.getReceipt()).thenReturn(RECEIPT);
        
        when(item.getId()).thenReturn(ITEM_ID);
        when(item.getFieldsMap()).thenReturn(fieldsMap);
        when(item.getNotifyBusinessFormUri()).thenReturn(BUSINESS_FORM_URI);
        when(item.getNotifyBusinessFormFilename()).thenReturn(BUSINESS_FORM_FILE_NAME);
        when(item.getNotifyCustomerFormUri()).thenReturn(CUSTOMER_FORM_URI);
        when(item.getNotifyCustomerFormFilename()).thenReturn(CUSTOMER_FORM_FILE_NAME);
        when(item.isPaid()).thenReturn(true);
        when(item.getQuantityPaid()).thenReturn(QUANTITY_PAID);

        when(statusLine.getStatusCode()).thenReturn(AttachmentService.OKAY_STATUS_CODE);
        when(businessResponse.getStatusLine()).thenReturn(statusLine);
        when(businessResponse.getEntity()).thenReturn(new StringEntity(BUSINESS_CONTENT));
        when(customerResponse.getStatusLine()).thenReturn(statusLine);
        when(customerResponse.getEntity()).thenReturn(new StringEntity(CUSTOMER_CONTENT));
        
        when(config.getNotifyFormRetryCount()).thenReturn(RETRY_COUNT);
        when(config.getNotifyFormRetryWait()).thenReturn(RETRY_WAIT);
        when(config.getNotifyFormTimeout()).thenReturn(TIMEOUT);
        
        service = new AttachmentService(config) {
            @Override
            protected HttpClient createClient() {
                return client;
            }
        };
    }
    
    @Test
    public void ignoreUnpaidItems() throws Exception {
        when(item.isPaid()).thenReturn(false);
        Map<String, byte[]> result = service.retrieve(order, NotifyType.BUSINESS);
        assertThat(result, not(hasKey(BUSINESS_FORM_FILE_NAME)));
    }
    
    @Test
    public void sendOrderAndItemDetailsToFormServiceAndReturnAsAttachmentsForBusiness() throws Exception {
        when(client.execute(argThat(postRequest(BUSINESS_FORM_URI, "quantityPaid=1&name=value&paid=paid+at&receipt=receipt"))))
            .thenReturn(businessResponse);
        
        Map<String, byte[]> result = service.retrieve(order, NotifyType.BUSINESS);
        assertThat(result, hasKey(ITEM_ID));
        assertThat(new String(result.get(ITEM_ID)), is(BUSINESS_CONTENT));
    }
    
    @Test
    public void sendOrderAndItemDetailsToFormServiceAndReturnAsAttachmentsForCustomer() throws Exception {
        when(client.execute(argThat(postRequest(CUSTOMER_FORM_URI, "quantityPaid=1&name=value&paid=paid+at&receipt=receipt"))))
            .thenReturn(customerResponse);
        
        Map<String, byte[]> result = service.retrieve(order, NotifyType.CUSTOMER);
        assertThat(result, hasKey(ITEM_ID));
        assertThat(new String(result.get(ITEM_ID)), is(CUSTOMER_CONTENT));
    }
    
    @Test
    public void retryWhenCannotDownload() throws Exception {
    	when(config.getNotifyFormRetryCount()).thenReturn(2);
        service = new AttachmentService(config) {
            @Override
            protected HttpClient createClient() {
                return client;
            }
        };
        
    	when(statusLine.getStatusCode()).thenReturn(500, AttachmentService.OKAY_STATUS_CODE);
        when(client.execute(argThat(postRequest(CUSTOMER_FORM_URI, "quantityPaid=1&name=value&paid=paid+at&receipt=receipt"))))
            .thenReturn(customerResponse);
        
        Map<String, byte[]> result = service.retrieve(order, NotifyType.CUSTOMER);
        assertThat(result, hasKey(ITEM_ID));
        assertThat(new String(result.get(ITEM_ID)), is(CUSTOMER_CONTENT));
        
        verify(client, times(2)).execute((argThat(postRequest(CUSTOMER_FORM_URI, "quantityPaid=1&name=value&paid=paid+at&receipt=receipt"))));
    }
    
    @Test(expected = IOException.class)
    public void throwExceptionWhencannotConnect() throws Exception {
        when(config.getNotifyFormRetryCount()).thenReturn(2);
        new AttachmentService(config).retrieve(order, NotifyType.CUSTOMER);
    }
    
    @Test(expected = IOException.class)
    public void throwExceptionWhenRetriesExhausted() throws Exception {
    	when(config.getNotifyFormRetryCount()).thenReturn(2);
        service = new AttachmentService(config) {
            @Override
            protected HttpClient createClient() {
                return client;
            }
        };
        
    	when(statusLine.getStatusCode()).thenReturn(500);
        when(client.execute(argThat(postRequest(CUSTOMER_FORM_URI, "quantityPaid=1&name=value&paid=paid+at&receipt=receipt"))))
            .thenReturn(customerResponse);
        
        try {
        	service.retrieve(order, NotifyType.CUSTOMER);
        } catch(IOException e) {
        	verify(client, times(2)).execute((argThat(postRequest(CUSTOMER_FORM_URI, "quantityPaid=1&name=value&paid=paid+at&receipt=receipt"))));
        	throw e;
        }
    }

    private Matcher<HttpPost> postRequest(final String uri, final String data) {
        return new BaseMatcher<HttpPost>() {
            @Override
            public boolean matches(Object item) {
                HttpPost post = (HttpPost)item;
                System.out.println("Request to: " + post.getURI().toString());
                if (!uri.equals(post.getURI().toString())) {
                	return false;
                }
                
                UrlEncodedFormEntity entity = (UrlEncodedFormEntity) post.getEntity();
                try {
                    String content = IOUtils.toString(entity.getContent());
                    System.out.println("Got content: " + content);
                    return data.equals(content);
                } catch (IOException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }

            @Override
            public void describeTo(Description description) {
            }
        };
    }
}
