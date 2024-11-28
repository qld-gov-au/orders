package au.gov.qld.pub.orders.service;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.ArgumentMatcher;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import au.gov.qld.pub.orders.entity.Item;
import au.gov.qld.pub.orders.entity.Order;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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
    private static final String BUSINESS_FORM_URI = "http://example.com/";
    private static final String CUSTOMER_FORM_FILE_NAME = "some customer form file name";
    private static final String CUSTOMER_FORM_URI = "http://example.com/";
    private static final String BUSINESS_FORM_FILE_NAME = "some business form file name";
    private static final String ITEM_ID = "some item id";

    AttachmentService service;

    @Mock Order order;
    @Mock Item item;
    @Mock Item item2;
    @Mock Item item3;
    @Mock
    HttpClient client;
    @Mock
    CloseableHttpResponse businessResponse;
    @Mock CloseableHttpResponse customerResponse;
    @Mock CloseableHttpResponse bundledCustomerResponse;
    @Mock ConfigurationService config;

    Map<String, String> fieldsMap =  Map.of("name", "value");

    @BeforeEach
    public void setUp() throws Exception {
        when(order.getUnbundledPaidItems()).thenReturn(asList(item));
        when(order.getPaid()).thenReturn(PAID_AT);
        when(order.getReceipt()).thenReturn(RECEIPT);

        setDefaultReturns(item, "");
        setDefaultReturns(item2, "2");
        setDefaultReturns(item3, "3");

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
//        when(item.getNotifyBusinessFormUri()).thenReturn(BUSINESS_FORM_URI); //UnnecessaryStubbingException
        when(item.getNotifyFormUri(NotifyType.BUSINESS)).thenReturn(BUSINESS_FORM_URI);
        when(item.getNotifyBusinessFormFilename()).thenReturn(BUSINESS_FORM_FILE_NAME);
//        when(item.getNotifyCustomerFormUri()).thenReturn(CUSTOMER_FORM_URI); //UnnecessaryStubbingException
        when(item.getNotifyFormUri(NotifyType.CUSTOMER)).thenReturn(CUSTOMER_FORM_URI);
        when(item.getNotifyCustomerFormFilename()).thenReturn(CUSTOMER_FORM_FILE_NAME);
//        when(item.isPaid()).thenReturn(true); //UnnecessaryStubbingException
        when(item.getQuantityPaid()).thenReturn(QUANTITY_PAID);
	}

    @Test
    public void sendOrderAndItemDetailsToFormServiceAndReturnAsAttachmentsForBusiness() throws Exception {
        when(businessResponse.getCode()).thenReturn(500, AttachmentService.OKAY_STATUS_CODE);
        when(businessResponse.getReasonPhrase()).thenReturn("500", "200");
        when(businessResponse.getEntity()).thenReturn(new StringEntity(BUSINESS_CONTENT));
        when(customerResponse.getCode()).thenReturn(500, AttachmentService.OKAY_STATUS_CODE);
        when(customerResponse.getReasonPhrase()).thenReturn("500", "200");
        when(customerResponse.getEntity()).thenReturn(new StringEntity(CUSTOMER_CONTENT));
        when(bundledCustomerResponse.getCode()).thenReturn(500, AttachmentService.OKAY_STATUS_CODE);
        when(bundledCustomerResponse.getReasonPhrase()).thenReturn("500", "200");
        when(bundledCustomerResponse.getEntity()).thenReturn(new StringEntity(BUNDLED_CUSTOMER_CONTENT));

        when(client.execute(ArgumentMatchers.argThat(new HttpPostMatcher(BUSINESS_FORM_URI, EXPECTED_FORM_DATA))))
            .thenReturn(businessResponse);

        List<FileAttachment> result = service.retrieve(order, NotifyType.BUSINESS);
        assertThat(result.size(), is(1));
        assertThat(new String(result.get(0).getName()), is(BUSINESS_FORM_FILE_NAME));
        assertThat(new String(result.get(0).getData()), is(BUSINESS_CONTENT));
    }

    @Test
    public void sendOrderAndItemDetailsToFormServiceAndReturnAsAttachmentsForBusinessForGivenItem() throws Exception {
        when(businessResponse.getCode()).thenReturn(500, AttachmentService.OKAY_STATUS_CODE);
        when(businessResponse.getReasonPhrase()).thenReturn("500", "200");
        when(businessResponse.getEntity()).thenReturn(new StringEntity(BUSINESS_CONTENT));
        when(customerResponse.getCode()).thenReturn(500, AttachmentService.OKAY_STATUS_CODE);
        when(customerResponse.getReasonPhrase()).thenReturn("500", "200");
        when(customerResponse.getEntity()).thenReturn(new StringEntity(CUSTOMER_CONTENT));
        when(bundledCustomerResponse.getCode()).thenReturn(500, AttachmentService.OKAY_STATUS_CODE);
        when(bundledCustomerResponse.getReasonPhrase()).thenReturn("500", "200");
        when(bundledCustomerResponse.getEntity()).thenReturn(new StringEntity(BUNDLED_CUSTOMER_CONTENT));

        when(client.execute(ArgumentMatchers.argThat(new HttpPostMatcher(BUSINESS_FORM_URI, EXPECTED_FORM_DATA))))
            .thenReturn(businessResponse);

        String result = new String(service.retrieve(order, NotifyType.BUSINESS, ITEM_ID).getData());
        assertThat(result, is(BUSINESS_CONTENT));
    }

    @Test
    public void throwExceptionWhenAttemptingToDownloadItemThatDoesNotExist() throws Exception {
        assertThrows(IllegalStateException.class, () -> {
//        when(client.execute(ArgumentMatchers.argThat(new HttpPostMatcher(BUSINESS_FORM_URI, EXPECTED_FORM_DATA))))
//            .thenReturn(businessResponse); //UnnecessaryStubbingException

            service.retrieve(order, NotifyType.BUSINESS, "bogus");
        });
    }

    @Test
    public void sendOrderAndItemDetailsToFormServiceAndReturnAsAttachmentsForCustomerAsSeparateItemsWhenNotBundled() throws Exception {

        when(businessResponse.getCode()).thenReturn(500, AttachmentService.OKAY_STATUS_CODE);
        when(businessResponse.getReasonPhrase()).thenReturn("500", "200");
        when(businessResponse.getEntity()).thenReturn(new StringEntity(BUSINESS_CONTENT));
        when(customerResponse.getCode()).thenReturn(500, AttachmentService.OKAY_STATUS_CODE);
        when(customerResponse.getReasonPhrase()).thenReturn("500", "200");
        when(customerResponse.getEntity()).thenReturn(new StringEntity(CUSTOMER_CONTENT));
        when(bundledCustomerResponse.getCode()).thenReturn(500, AttachmentService.OKAY_STATUS_CODE);
        when(bundledCustomerResponse.getReasonPhrase()).thenReturn("500", "200");
        when(bundledCustomerResponse.getEntity()).thenReturn(new StringEntity(BUNDLED_CUSTOMER_CONTENT));


        when(client.execute(ArgumentMatchers.argThat(new HttpPostMatcher(CUSTOMER_FORM_URI, EXPECTED_FORM_DATA))))
            .thenReturn(customerResponse);

        List<FileAttachment> result = service.retrieve(order, NotifyType.CUSTOMER);
        assertThat(result.size(), is(1));
        assertThat(new String(result.get(0).getName()), is(CUSTOMER_FORM_FILE_NAME));
        assertThat(new String(result.get(0).getData()), is(CUSTOMER_CONTENT));
    }

    @Test
    public void sendOrderAndItemDetailsBundledPerGroupAndReturnOneAttachmentWhenItemsShouldBeBundled() throws Exception {

        when(businessResponse.getCode()).thenReturn(500, AttachmentService.OKAY_STATUS_CODE);
        when(businessResponse.getReasonPhrase()).thenReturn("500", "200");
        when(businessResponse.getEntity()).thenReturn(new StringEntity(BUSINESS_CONTENT));
        when(customerResponse.getCode()).thenReturn(500, AttachmentService.OKAY_STATUS_CODE);
        when(customerResponse.getReasonPhrase()).thenReturn("500", "200");
        when(customerResponse.getEntity()).thenReturn(new StringEntity(CUSTOMER_CONTENT));
        when(bundledCustomerResponse.getCode()).thenReturn(500, AttachmentService.OKAY_STATUS_CODE);
        when(bundledCustomerResponse.getReasonPhrase()).thenReturn("500", "200");
        when(bundledCustomerResponse.getEntity()).thenReturn(new StringEntity(BUNDLED_CUSTOMER_CONTENT));


        when(order.getUnbundledPaidItems()).thenReturn(asList(item));
    	when(client.execute(ArgumentMatchers.argThat(new HttpPostMatcher(CUSTOMER_FORM_URI, EXPECTED_FORM_DATA))))
    		.thenReturn(customerResponse,customerResponse);
    	when(order.getBundledPaidItems()).thenReturn(asList(item2, item3));
    	when(client.execute(ArgumentMatchers.argThat(new HttpPostMatcher(CUSTOMER_FORM_URI, EXPECTED_FORM_DATA_BUNDLED))))
    		.thenReturn(bundledCustomerResponse,bundledCustomerResponse);

    	List<FileAttachment> result = service.retrieve(order, NotifyType.CUSTOMER);
    	assertThat(result.size(), is(2));
    	assertThat(new String(result.get(0).getData()), is(BUNDLED_CUSTOMER_CONTENT));
    	assertThat(new String(result.get(1).getData()), is(CUSTOMER_CONTENT));

    	String itemResult = new String(service.retrieve(order, NotifyType.CUSTOMER, item2.getId()).getData());
    	assertThat(itemResult, is(BUNDLED_CUSTOMER_CONTENT));
    	itemResult = new String(service.retrieve(order, NotifyType.CUSTOMER, item3.getId()).getData());
    	assertThat(itemResult, is(BUNDLED_CUSTOMER_CONTENT));

    	itemResult = new String(service.retrieve(order, NotifyType.CUSTOMER, item.getId()).getData());
    	assertThat(itemResult, is(CUSTOMER_CONTENT));
    }

    @Test
    public void retryWhenCannotDownload() throws Exception {
        when(businessResponse.getCode()).thenReturn(500, AttachmentService.OKAY_STATUS_CODE);
        when(businessResponse.getReasonPhrase()).thenReturn("500", "200");
        when(businessResponse.getEntity()).thenReturn(new StringEntity(BUSINESS_CONTENT));
        when(customerResponse.getCode()).thenReturn(500, AttachmentService.OKAY_STATUS_CODE);
        when(customerResponse.getReasonPhrase()).thenReturn("500", "200");
        when(customerResponse.getEntity()).thenReturn(new StringEntity(CUSTOMER_CONTENT));
        when(bundledCustomerResponse.getCode()).thenReturn(500, AttachmentService.OKAY_STATUS_CODE);
        when(bundledCustomerResponse.getReasonPhrase()).thenReturn("500", "200");
        when(bundledCustomerResponse.getEntity()).thenReturn(new StringEntity(BUNDLED_CUSTOMER_CONTENT));


        when(config.getNotifyFormRetryCount()).thenReturn(2);
        service = new AttachmentService(config) {
            @Override
            protected HttpClient createClient() {
                return client;
            }
        };

        when(client.execute(ArgumentMatchers.argThat(new HttpPostMatcher(CUSTOMER_FORM_URI, EXPECTED_FORM_DATA))))
            .thenReturn(customerResponse);

        List<FileAttachment> result = service.retrieve(order, NotifyType.CUSTOMER);
        assertThat(result.size(), is(1));
        assertThat(new String(result.get(0).getData()), is(CUSTOMER_CONTENT));

        verify(client, times(2)).execute((ArgumentMatchers.argThat(new HttpPostMatcher(CUSTOMER_FORM_URI, EXPECTED_FORM_DATA))));
    }

    @Test
    public void retryWhenCannotDownloadWithIOException() throws Exception {

        when(config.getNotifyFormRetryCount()).thenReturn(2);
        service = new AttachmentService(config) {
            @Override
            protected HttpClient createClient() {
                return client;
            }
        };

        doThrow(new SocketTimeoutException("expected")).when(client).execute(ArgumentMatchers.argThat(new HttpPostMatcher(CUSTOMER_FORM_URI, EXPECTED_FORM_DATA)));
        try {
        	service.retrieve(order, NotifyType.CUSTOMER);
        	fail("Should have ran out of attempts");
        } catch (IOException e) {
        	assertThat(e.getMessage(), containsString("Retries exhausted"));
        }
        verify(client, times(3)).execute((ArgumentMatchers.argThat(new HttpPostMatcher(CUSTOMER_FORM_URI, EXPECTED_FORM_DATA))));
    }

    @Test
    public void throwExceptionWhencannotConnect() throws Exception {
        when(config.getNotifyFormRetryCount()).thenReturn(0);
        service = new AttachmentService(config) {
            @Override
            protected HttpClient createClient() {
                return client;
            }
        };

        assertThrows(IOException.class, () -> {
            service.retrieve(order, NotifyType.CUSTOMER);
        });
    }

    @Test
    public void throwExceptionWhenRetriesExhausted() throws Exception {
        when(customerResponse.getCode()).thenReturn(500);
        when(customerResponse.getReasonPhrase()).thenReturn("500 Mock Error - customerResponse");
        when(customerResponse.getEntity()).thenReturn(new StringEntity(CUSTOMER_CONTENT));


        assertThrows(IOException.class, () -> {
            when(config.getNotifyFormRetryCount()).thenReturn(2);
            service = new AttachmentService(config) {
                @Override
                protected HttpClient createClient() {
                    return client;
                }
            };

            when(client.execute(ArgumentMatchers.argThat(new HttpPostMatcher(CUSTOMER_FORM_URI, EXPECTED_FORM_DATA))))
                .thenReturn(customerResponse);

            try {
                service.retrieve(order, NotifyType.CUSTOMER);
            } catch (IOException e) {
                verify(client, times(3)).execute((ArgumentMatchers.argThat(new HttpPostMatcher(CUSTOMER_FORM_URI, EXPECTED_FORM_DATA))));
                throw e;
            }
        });
    }


    private class HttpPostMatcher implements ArgumentMatcher<HttpPost>  {

        private String uri;
        private String data;
        private HttpPostMatcher(String uri, String data) {
            this.uri = uri;
            this.data = data;
        }

        @Override
        public boolean matches(HttpPost post) {
            if (post == null) {
                return false;
            }
            post.setAbsoluteRequestUri(true);
            boolean uriMatched = uri.equals(post.getRequestUri().toString());
            if (!uriMatched) {
                System.out.println("Request to:      " + post.getRequestUri().toString());
                System.out.println("Needs to map to: " + uri);
                return false;
            } else {
                System.out.println("Got URI Matched mockito");
            }

            UrlEncodedFormEntity entity = (UrlEncodedFormEntity) post.getEntity();
            try {
                String content = IOUtils.toString(entity.getContent(), Charset.defaultCharset());
                boolean matched = data.equals(content);
                if (matched) {
                    System.out.println("Got Matched mockito");
                    return matched;
                } else {
                    System.out.println("Got\ncontent: " + content);
                    System.out.println("Wanted\ncontent: " + data);
                }
                return data.equals(content);
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }

        @Override
        public String toString() {
            return String.format("HttpPostMatcher(uri=%s, formData=%s)", uri, data);
        }
    }
}
