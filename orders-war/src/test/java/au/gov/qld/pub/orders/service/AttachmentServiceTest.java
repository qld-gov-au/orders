package au.gov.qld.pub.orders.service;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
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
    private static final String EXPECTED_FORM_DATA = "name=value&quantityPaid=1&productGroup=some+product+group&productId=some+product+id"
    		+ "&priceTotal=579&priceGst=456&priceExGst=123&paid=paid+at&receipt=receipt";
    private static final String EXPECTED_FORM_DATA_BUNDLED = "name=value&quantityPaid=1&productGroup=some+product+group&productId=some+product+id2"
    		+ "&priceTotal=579&priceGst=456&priceExGst=123&name-1=value&quantityPaid-1=1&productGroup-1=some+product+group&productId-1=some+product+id3"
    		+ "&priceTotal-1=579&priceGst-1=456&priceExGst-1=123&paid=paid+at&receipt=receipt";
    private static final String PRICE_GST = "456";
    private static final String PRICE_EX_GST = "123";
    private static final Integer RETRY_COUNT = 1;
    private static final Integer RETRY_WAIT = 1;
    private static final Integer TIMEOUT = 1;
    private static final String PAID_AT = "paid at";
    private static final String RECEIPT = "receipt";
    private static final String PRODUCT_ID = "some product id";
    private static final String PRODUCT_GROUP = "some product group";
    private static final String QUANTITY_PAID = "1";
    private static final String BUSINESS_CONTENT = "business content";
    private static final String CUSTOMER_CONTENT = "customer content";
    private static final String BUNDLED_CUSTOMER_CONTENT = "bundled customer content";
    private static final String BUSINESS_FORM_URI = "http://example.com";
    private static final String CUSTOMER_FORM_FILE_NAME = "some customer form file name";
    private static final String CUSTOMER_FORM_URI = "http://example.com";
    private static final String BUSINESS_FORM_FILE_NAME = "some business form file name";
    private static final String ITEM_ID = "some item id";

    AttachmentService service;
    
    @Mock Order order;
    @Mock Item item;
    @Mock Item item2;
    @Mock Item item3;
    @Mock HttpClient client;
    @Mock CloseableHttpResponse businessResponse;
    @Mock CloseableHttpResponse customerResponse;
    @Mock CloseableHttpResponse bundledCustomerResponse;
    @Mock StatusLine statusLine;
    @Mock ConfigurationService config;
    
    Map<String, String> fieldsMap = ImmutableMap.of("name", "value");
    
    @Before
    public void setUp() throws Exception {
        when(order.getUnbundledPaidItems()).thenReturn(asList(item));
        when(order.getPaid()).thenReturn(PAID_AT);
        when(order.getReceipt()).thenReturn(RECEIPT);

        setDefaultReturns(item, "");
        setDefaultReturns(item2, "2");
        setDefaultReturns(item3, "3");

        when(statusLine.getStatusCode()).thenReturn(AttachmentService.OKAY_STATUS_CODE);
        when(businessResponse.getStatusLine()).thenReturn(statusLine);
        when(businessResponse.getEntity()).thenReturn(new StringEntity(BUSINESS_CONTENT));
        when(customerResponse.getStatusLine()).thenReturn(statusLine);
        when(customerResponse.getEntity()).thenReturn(new StringEntity(CUSTOMER_CONTENT));
        when(bundledCustomerResponse.getStatusLine()).thenReturn(statusLine);
        when(bundledCustomerResponse.getEntity()).thenReturn(new StringEntity(BUNDLED_CUSTOMER_CONTENT));
        
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

	private void setDefaultReturns(Item item, String suffix) {
		when(item.getProductGroup()).thenReturn(PRODUCT_GROUP);
        when(item.getProductId()).thenReturn(PRODUCT_ID + suffix);
        when(item.getPriceExGst()).thenReturn(PRICE_EX_GST);
        when(item.getPriceGst()).thenReturn(PRICE_GST);
        when(item.getId()).thenReturn(ITEM_ID + suffix);
        when(item.getFieldsMap()).thenReturn(fieldsMap);
        when(item.getNotifyBusinessFormUri()).thenReturn(BUSINESS_FORM_URI);
        when(item.getNotifyFormUri(NotifyType.BUSINESS)).thenReturn(BUSINESS_FORM_URI);
        when(item.getNotifyBusinessFormFilename()).thenReturn(BUSINESS_FORM_FILE_NAME);
        when(item.getNotifyCustomerFormUri()).thenReturn(CUSTOMER_FORM_URI);
        when(item.getNotifyFormUri(NotifyType.CUSTOMER)).thenReturn(CUSTOMER_FORM_URI);
        when(item.getNotifyCustomerFormFilename()).thenReturn(CUSTOMER_FORM_FILE_NAME);
        when(item.isPaid()).thenReturn(true);
        when(item.getQuantityPaid()).thenReturn(QUANTITY_PAID);
	}
    
    @Test
    public void sendOrderAndItemDetailsToFormServiceAndReturnAsAttachmentsForBusiness() throws Exception {
        when(client.execute(argThat(postRequest(BUSINESS_FORM_URI, EXPECTED_FORM_DATA))))
            .thenReturn(businessResponse);
        
        List<EmailAttachment> result = service.retrieve(order, NotifyType.BUSINESS);
        assertThat(result.size(), is(1));
        assertThat(new String(result.get(0).getName()), is(BUSINESS_FORM_FILE_NAME));
        assertThat(new String(result.get(0).getData()), is(BUSINESS_CONTENT));
    }
    
    @Test
    public void sendOrderAndItemDetailsToFormServiceAndReturnAsAttachmentsForBusinessForGivenItem() throws Exception {
        when(client.execute(argThat(postRequest(BUSINESS_FORM_URI, EXPECTED_FORM_DATA))))
            .thenReturn(businessResponse);
        
        byte[] result = service.retrieve(order, NotifyType.BUSINESS, ITEM_ID);
        assertThat(new String(result), is(BUSINESS_CONTENT));
    }
    
    @Test
    public void returnEmptyDataWhenAttemptingToDownloadItemThatDoesNotExist() throws Exception {
        when(client.execute(argThat(postRequest(BUSINESS_FORM_URI, EXPECTED_FORM_DATA))))
            .thenReturn(businessResponse);
        
        byte[] result = service.retrieve(order, NotifyType.BUSINESS, "bogus");
        assertThat(result.length, is(0));
    }
    
    @Test
    public void sendOrderAndItemDetailsToFormServiceAndReturnAsAttachmentsForCustomerAsSeparateItemsWhenNotBundled() throws Exception {
        when(client.execute(argThat(postRequest(CUSTOMER_FORM_URI, EXPECTED_FORM_DATA))))
            .thenReturn(customerResponse);
        
        List<EmailAttachment> result = service.retrieve(order, NotifyType.CUSTOMER);
        assertThat(result.size(), is(1));
        assertThat(new String(result.get(0).getName()), is(CUSTOMER_FORM_FILE_NAME));
        assertThat(new String(result.get(0).getData()), is(CUSTOMER_CONTENT));
    }
    
    @Test
    public void sendOrderAndItemDetailsBundledPerGroupAndReturnOneAttachmentWhenItemsShouldBeBundled() throws Exception {
    	when(order.getUnbundledPaidItems()).thenReturn(asList(item));
    	when(client.execute(argThat(postRequest(CUSTOMER_FORM_URI, EXPECTED_FORM_DATA))))
    		.thenReturn(customerResponse);
    	when(order.getBundledPaidItems()).thenReturn(asList(item2, item3));
    	when(client.execute(argThat(postRequest(CUSTOMER_FORM_URI, EXPECTED_FORM_DATA_BUNDLED))))
    		.thenReturn(bundledCustomerResponse);
    	
    	List<EmailAttachment> result = service.retrieve(order, NotifyType.CUSTOMER);
    	assertThat(result.size(), is(2));
    	assertThat(new String(result.get(0).getData()), is(BUNDLED_CUSTOMER_CONTENT));
    	assertThat(new String(result.get(1).getData()), is(CUSTOMER_CONTENT));
    	
    	String itemResult = new String(service.retrieve(order, NotifyType.CUSTOMER, item2.getId()));
    	assertThat(itemResult, is(BUNDLED_CUSTOMER_CONTENT));
    	itemResult = new String(service.retrieve(order, NotifyType.CUSTOMER, item3.getId()));
    	assertThat(itemResult, is(BUNDLED_CUSTOMER_CONTENT));
    	
    	itemResult = new String(service.retrieve(order, NotifyType.CUSTOMER, item.getId()));
    	assertThat(itemResult, is(CUSTOMER_CONTENT));
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
        when(client.execute(argThat(postRequest(CUSTOMER_FORM_URI, EXPECTED_FORM_DATA))))
            .thenReturn(customerResponse);
        
        List<EmailAttachment> result = service.retrieve(order, NotifyType.CUSTOMER);
        assertThat(result.size(), is(1));
        assertThat(new String(result.get(0).getData()), is(CUSTOMER_CONTENT));
        
        verify(client, times(2)).execute((argThat(postRequest(CUSTOMER_FORM_URI, EXPECTED_FORM_DATA))));
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
        when(client.execute(argThat(postRequest(CUSTOMER_FORM_URI, EXPECTED_FORM_DATA))))
            .thenReturn(customerResponse);
        
        try {
            service.retrieve(order, NotifyType.CUSTOMER);
        } catch(IOException e) {
            verify(client, times(2)).execute((argThat(postRequest(CUSTOMER_FORM_URI, EXPECTED_FORM_DATA))));
            throw e;
        }
    }

    private Matcher<HttpPost> postRequest(final String uri, final String data) {
        return new BaseMatcher<HttpPost>() {
            @Override
            public boolean matches(Object item) {
                HttpPost post = (HttpPost)item;
                if (post == null) {
                	return false;
                }
                
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
