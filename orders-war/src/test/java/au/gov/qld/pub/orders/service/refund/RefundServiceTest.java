package au.gov.qld.pub.orders.service.refund;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import au.gov.qld.pub.orders.dao.RefundItemDAO;
import au.gov.qld.pub.orders.entity.RefundItem;
import au.gov.qld.pub.orders.entity.RefundState;
import au.gov.qld.pub.orders.service.ConfigurationService;
import au.gov.qld.pub.orders.service.refund.dto.LineItem;
import au.gov.qld.pub.orders.service.refund.dto.RefundQueryResponse;
import au.gov.qld.pub.orders.service.refund.dto.RefundRequestResponse;
import au.gov.qld.pub.orders.service.ws.SOAPClient;

@RunWith(MockitoJUnitRunner.class)
public class RefundServiceTest {
	
	private static final String QUERY_REQUEST = "refund query request";
	private static final String REQUEST_REQUEST = "refund request request";
	private static final String PASSWORD = "some password";
	private static final String USERNAME = "some username";
	private static final String QUERY_RESPONSE = "query response";
	private static final String REQUEST_RESPONSE = "request response";
	private static final int PAPI_LINE_ITEM_ID = 1234;
	private static final String ORDERLINE_ID = "some orderline id";
	
	@Mock RefundItemDAO refundItemDAO;
	@Mock ConfigurationService configurationService;
	@Mock SOAPClient client;
	@Mock RefundResponseParser refundResponseParser;
	@Mock RefundRequestBuilder refundRequestBuilder;
	@Mock RefundItem refundItem;
	@Mock RefundItem associatedRefundItem;
	@Mock RefundRequestResponse requestResponse;
	@Mock RefundQueryResponse queryResponse;
	@Mock LineItem lineItem;
	@Mock LineItem associatedLineItem;
	
	RefundService service;

	@Before
	public void setUp() throws Exception {
		when(lineItem.getPapiLineItemId()).thenReturn(PAPI_LINE_ITEM_ID);
		when(lineItem.getOrderLineId()).thenReturn(ORDERLINE_ID);
		when(associatedLineItem.getOrderLineId()).thenReturn("associated" + ORDERLINE_ID);
		when(associatedLineItem.getPapiLineItemId()).thenReturn(PAPI_LINE_ITEM_ID + 1);
		when(queryResponse.getLineItem()).thenReturn(asList(lineItem, associatedLineItem));
		
		when(configurationService.getServiceWsUsername()).thenReturn(USERNAME);
		when(configurationService.getServiceWsPassword()).thenReturn(PASSWORD);
		service = new RefundService(refundItemDAO, configurationService, refundResponseParser, refundRequestBuilder);
		service.setClient(client);
	}
	
	@Test
	public void shouldIgnorePreparingRefundsNotNew() {
//		when(refundItem.getRefundState()).thenReturn(RefundState.ERROR); //UnnecessaryStubbingException
		service.refundNewItems();
		verifyNoInteractions(client);
		verify(refundItem, never()).updated();
	}
	
	@Test
	public void shouldPrepareAndSendRefundRequestForEachNew() throws Exception {
		when(refundItem.getRefundState()).thenReturn(RefundState.NEW);
		when(refundItemDAO.findByRefundState(RefundState.NEW)).thenReturn(asList(refundItem));
		when(refundRequestBuilder.buildQuery(refundItem)).thenReturn(QUERY_REQUEST);
//		when(refundRequestBuilder.buildRequest(argThat(hasProperty("papiLineItemId", is(PAPI_LINE_ITEM_ID))))).thenReturn(REQUEST_REQUEST); //UnnecessaryStubbingException
//		when(refundResponseParser.parseRequestResponse(REQUEST_RESPONSE)).thenReturn(requestResponse); //UnnecessaryStubbingException
		when(refundItemDAO.findByOrderLineIdAndRefundState(ORDERLINE_ID, RefundState.NEW)).thenReturn(asList(refundItem));
		when(refundItemDAO.findByOrderLineIdAndRefundState("associated" + ORDERLINE_ID, RefundState.NEW)).thenReturn(asList(associatedRefundItem));
		when(refundResponseParser.parseQueryResponse(QUERY_RESPONSE)).thenReturn(queryResponse);
		when(client.sendRequest(USERNAME, PASSWORD.getBytes(StandardCharsets.UTF_8), RefundService.REFUND_QUERY_NS, QUERY_REQUEST)).thenReturn(QUERY_RESPONSE);
		
		service.refundNewItems();
		
		verify(client).sendRequest(USERNAME, PASSWORD.getBytes(StandardCharsets.UTF_8), RefundService.REFUND_QUERY_NS, QUERY_REQUEST);
		verify(refundItem).setPapiLineItemId(PAPI_LINE_ITEM_ID);
		verify(refundItem).updated();
		verify(associatedRefundItem).setPapiLineItemId(PAPI_LINE_ITEM_ID + 1);
		verify(associatedRefundItem).updated();
		verify(refundItemDAO).save(refundItem);
		verify(refundItemDAO).save(associatedRefundItem);
		// TODO:
		// verify(client).sendRequest(USERNAME, PASSWORD.getBytes(StandardCharsets.UTF_8), RefundService.REFUND_REQUEST_NS, REQUEST_REQUEST);
	}
}
