package au.gov.qld.pub.orders.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

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
	
	NoticeToPayService service;

	@Mock PaymentInformationService paymentInformationService;
	@Mock SOAPClient soapClient;
	@Mock NoticeToPayDAO noticeToPayDAO;
	@Mock RequestBuilder requestBuilder;
	@Mock PaymentInformation paymentInformation;
	@Mock ConfigurationService config;
	
	@Before
    public void setUp() throws Exception {
		when(config.getServiceWsEndpoint()).thenReturn(ENDPOINT);
		when(config.getServiceWsPassword()).thenReturn(PASSWORD);
		when(config.getServiceWsUsername()).thenReturn(USERNAME);
		when(requestBuilder.noticeToPay(eq(paymentInformation), anyString(), eq(SOURCE_URL))).thenReturn(REQUEST);
		when(soapClient.sendRequest(USERNAME, PASSWORD.getBytes("UTF-8"), NoticeToPayService.NS, REQUEST)).thenReturn(RESPONSE);
		when(paymentInformation.getAmountOwingInCents()).thenReturn(OWING);
		when(paymentInformationService.fetch(SOURCE_ID)).thenReturn(paymentInformation);
		
		service = new NoticeToPayService(config, paymentInformationService, noticeToPayDAO, requestBuilder) {
			protected SOAPClient getSOAPClient(String endpoint) {
				return ENDPOINT.equals(endpoint) ? soapClient : null;
			}
		};
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
			org.junit.Assert.fail("Should have thrown exception");
		} catch (ServiceException e) {
			verifyZeroInteractions(noticeToPayDAO);
		}
	}
	
	@Test
	public void throwExceptionWhenInvalidResponse() throws Exception {
		when(soapClient.sendRequest(USERNAME, PASSWORD.getBytes("UTF-8"), NoticeToPayService.NS, REQUEST)).thenReturn("bogus");
		try {
			service.create(SOURCE_ID, SOURCE_URL);
			org.junit.Assert.fail("Should have thrown exception");
		} catch (ServiceException e) {
			verifyZeroInteractions(noticeToPayDAO);
		}
	}
}
