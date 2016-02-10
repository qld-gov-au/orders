package au.gov.qld.pub.orders.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import au.gov.qld.pub.orders.dao.NoticeToPayDAO;
import au.gov.qld.pub.orders.entity.NoticeToPay;
import au.gov.qld.pub.orders.service.ws.RequestBuilder;
import au.gov.qld.pub.orders.service.ws.SOAPClient;

@RunWith(MockitoJUnitRunner.class)
public class NoticeToPayServiceTest {
    private static final String SOURCE_ID = "some id";
    private static final String REQUEST = "some request";
    private static final String REDIRECT = "some redirect";
    private static final String RESPONSE = "<redirectUrl>" + REDIRECT + "</redirectUrl>";
    private static final String USERNAME = "some username";
    private static final String PASSWORD = "some password";
    private static final String ENDPOINT = "some endpoint";
    private static final long OWING = 123l;
    private static final String SOURCE_URL = "some source";
    private static final String NOTICE_TO_PAY_ID = "some notice to pay id";
    private static final String RECEIPT_NUMBER = "some receipt number";
    private static final String UNPAID_QUERY_RESPONSE = "<status>NOT_PAID</status>";
    private static final String FAILED_QUERY_RESPONSE = "<status>FAILED</status>";
    private static final String PAID_QUERY_RESPONSE = "<status>PAID</status><receiptNumber>" + RECEIPT_NUMBER + "</receiptNumber>";
    private static final String NTP_QUERY = "some ntp query";
    
    NoticeToPayService service;

    @Mock PaymentInformationService paymentInformationService;
    @Mock SOAPClient soapClient;
    @Mock NoticeToPayDAO noticeToPayDAO;
    @Mock RequestBuilder requestBuilder;
    @Mock PaymentInformation paymentInformation;
    @Mock ConfigurationService config;
    @Mock NoticeToPay noticeToPay;
    
    @Before
    public void setUp() throws Exception {
        DateTimeUtils.setCurrentMillisFixed(new DateTime().toDate().getTime());
        when(config.getServiceWsEndpoint()).thenReturn(ENDPOINT);
        when(config.getNoticeToPayServiceWsPassword()).thenReturn(PASSWORD);
        when(config.getNoticeToPayServiceWsUsername()).thenReturn(USERNAME);
        when(requestBuilder.noticeToPay(eq(paymentInformation), anyString(), eq(SOURCE_URL))).thenReturn(REQUEST);
        when(requestBuilder.noticeToPayQuery(NOTICE_TO_PAY_ID)).thenReturn(NTP_QUERY);
        when(soapClient.sendRequest(USERNAME, PASSWORD.getBytes("UTF-8"), NoticeToPayService.NS, REQUEST)).thenReturn(RESPONSE);
        when(soapClient.sendRequest(USERNAME, PASSWORD.getBytes("UTF-8"), NoticeToPayService.NS, NTP_QUERY)).thenReturn(PAID_QUERY_RESPONSE);
        when(paymentInformation.getAmountOwingInCents()).thenReturn(OWING);
        when(paymentInformation.getReference()).thenReturn(SOURCE_ID);
        when(paymentInformationService.fetch(SOURCE_ID)).thenReturn(paymentInformation);
        when(noticeToPayDAO.findOne(NOTICE_TO_PAY_ID)).thenReturn(noticeToPay);
        
        service = new NoticeToPayService(config, paymentInformationService, noticeToPayDAO, requestBuilder) {
            protected SOAPClient getSOAPClient(String endpoint) {
                return ENDPOINT.equals(endpoint) ? soapClient : null;
            }
        };
    }
    
    @After
    public void tearDown() {
        DateTimeUtils.setCurrentMillisSystem();
    }
    
    @Test
    public void returnRedirectForFetchedAndSavedPaymentInformation() throws Exception {
        String redirect = service.create(SOURCE_ID, SOURCE_URL);
        assertThat(redirect, is(REDIRECT));
        verify(noticeToPayDAO).save((NoticeToPay)argThat(hasProperty("paymentInformationId", equalTo(SOURCE_ID))));
    }
    
    @Test
    public void throwExceptionIfNoOwingAmount() throws Exception {
        when(paymentInformation.getAmountOwingInCents()).thenReturn(0l);
        try {
            service.create(SOURCE_ID, SOURCE_URL);
            fail("Should have thrown exception");
        } catch (ServiceException e) {
            verifyZeroInteractions(soapClient);
            verifyZeroInteractions(noticeToPayDAO);
        }
    }
    
    @Test
    public void throwExceptionIfRecentlyPaid() throws Exception {
        when(noticeToPayDAO.existsByPaymentInformationIdAndNotifiedAtAfter(SOURCE_ID, new DateTime().minusHours(1).toDate())).thenReturn(true);
        try {
            service.create(SOURCE_ID, SOURCE_URL);
            fail("Should have thrown exception");
        } catch (ServiceException e) {
            verifyZeroInteractions(soapClient);
        }
    }
    
    @Test
    public void throwExceptionWhenInvalidResponseFromRequest() throws Exception {
        when(soapClient.sendRequest(USERNAME, PASSWORD.getBytes("UTF-8"), NoticeToPayService.NS, REQUEST)).thenReturn("bogus");
        try {
            service.create(SOURCE_ID, SOURCE_URL);
            fail("Should have thrown exception");
        } catch (ServiceException e) {
            verify(noticeToPayDAO).save((NoticeToPay)argThat(hasProperty("paymentInformationId", equalTo(SOURCE_ID))));
        }
    }
    
    @Test
    public void setNoticeToPayWithPaymentDetailsOnNotifyAfterCheckingStatus() throws Exception {
        service.notifyPayment(NOTICE_TO_PAY_ID);
        
        verify(noticeToPay).setReceiptNumber(RECEIPT_NUMBER);
        verify(noticeToPay).setNotifiedAt(argThat(isA(Date.class)));
        verify(noticeToPayDAO).save(noticeToPay);
    }
    
    @Test
    public void doNotSetNoticeToPayWithPaymentDetailsOnNotifyWhenAlreadyNotified() throws Exception {
        when(noticeToPay.getNotifiedAt()).thenReturn(new Date());
        service.notifyPayment(NOTICE_TO_PAY_ID);
        
        verifyZeroInteractions(requestBuilder);
        verifyZeroInteractions(soapClient);
        verify(noticeToPayDAO, never()).save(argThat(isA(NoticeToPay.class)));
    }
    
    @Test
    public void throwExceptionIfNotifyingUnknownId() throws ServiceException {
        try {
            service.notifyPayment("bogus");
            fail();
        } catch (ServiceException e) {
            verifyZeroInteractions(noticeToPay);
            verifyZeroInteractions(requestBuilder);
            verifyZeroInteractions(soapClient);
        }
    }
    
    @Test
    public void throwExceptionIfNotifyingUnpaid() throws Exception {
        when(soapClient.sendRequest(USERNAME, PASSWORD.getBytes("UTF-8"), NoticeToPayService.NS, NTP_QUERY)).thenReturn(UNPAID_QUERY_RESPONSE);
        try {
            service.notifyPayment(NOTICE_TO_PAY_ID);
            fail();
        } catch (ServiceException e) {
            verify(noticeToPayDAO, never()).save(argThat(isA(NoticeToPay.class)));
        }
    }
    
    @Test
    public void ignoreNotificationOnFailed() throws Exception {
        when(soapClient.sendRequest(USERNAME, PASSWORD.getBytes("UTF-8"), NoticeToPayService.NS, NTP_QUERY)).thenReturn(FAILED_QUERY_RESPONSE);
        service.notifyPayment(NOTICE_TO_PAY_ID);
        verify(noticeToPayDAO, never()).save(argThat(isA(NoticeToPay.class)));
    }
    
}
