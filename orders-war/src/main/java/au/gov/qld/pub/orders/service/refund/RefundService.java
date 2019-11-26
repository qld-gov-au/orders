package au.gov.qld.pub.orders.service.refund;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import au.gov.qld.pub.orders.dao.RefundItemDAO;
import au.gov.qld.pub.orders.entity.RefundItem;
import au.gov.qld.pub.orders.entity.RefundState;
import au.gov.qld.pub.orders.service.ConfigurationService;
import au.gov.qld.pub.orders.service.ServiceException;
import au.gov.qld.pub.orders.service.refund.dto.RefundQueryResponse;
import au.gov.qld.pub.orders.service.refund.dto.RefundRequestResponse;
import au.gov.qld.pub.orders.service.ws.SOAPClient;

@Service
public class RefundService {
	private static final Logger LOG = LoggerFactory.getLogger(RefundService.class);
	static final String REFUND_QUERY_NS = "http://smartservice.qld.gov.au/payment/schemas/refund_query_1_0";
	static final String REFUND_REQUEST_NS = "http://smartservice.qld.gov.au/payment/schemas/refund_request_1_0";
	
    private final RefundItemDAO refundItemDAO;
	private final byte[] passwordBytes;
	private final String username;
	private final RefundResponseParser refundResponseParser;
	private final RefundRequestBuilder refundRequestBuilder;
	
	private SOAPClient client;

    @Autowired
    public RefundService(RefundItemDAO refundItemDAO, ConfigurationService configurationService, RefundResponseParser refundResponseParser,
    		RefundRequestBuilder refundRequestBuilder) throws IOException {
        this.refundItemDAO = refundItemDAO;
		this.refundResponseParser = refundResponseParser;
		this.refundRequestBuilder = refundRequestBuilder;
        String password = configurationService.getServiceWsPassword();
        String endpoint = configurationService.getServiceWsEndpoint();
        this.username = configurationService.getServiceWsUsername();
        this.passwordBytes = password.getBytes("UTF-8");
        this.client = new SOAPClient(endpoint);
    }
    
    public void setClient(SOAPClient client) {
    	this.client = client;
    }
    
    public void refundNewItems() {
    	refundItemDAO.findByRefundState(RefundState.NEW).forEach(refundItem -> prepareRefundNewItem(refundItem));
    	// TODO:
    	// refundItemDAO.findByRefundState(RefundState.PREPARED).forEach(refundItem -> refundPreparedItem(refundItem));
    }

    @Transactional
	public void prepareRefundNewItem(RefundItem refundItem) {
    	if (!RefundState.NEW.equals(refundItem.getRefundState())) {
    		LOG.debug("Ignoring: {} with state: {}", refundItem.getId(), refundItem.getRefundState());
    		return;
    	}
    	
    	LOG.info("Preparing: {} with receipt: {} for refund.", refundItem.getId(), refundItem.getPapiReceiptNumber());
		try {
			String request = refundRequestBuilder.buildQuery(refundItem);
			String wsResponse = client.sendRequest(username, passwordBytes, REFUND_QUERY_NS, request);
			RefundQueryResponse response = refundResponseParser.parseQueryResponse(wsResponse);
			response.getLineItem().stream().forEach(lineItem -> {
				List<RefundItem> associatedRefundItems = refundItemDAO.findByOrderLineIdAndRefundState(lineItem.getOrderLineId(), RefundState.NEW);
				associatedRefundItems.stream().forEach(associatedRefund -> {
					associatedRefund.setRefundState(RefundState.PREPARED);
					associatedRefund.setPapiLineItemId(lineItem.getPapiLineItemId());
					associatedRefund.updated();
					refundItemDAO.save(associatedRefund);
					LOG.info("Prepared refund: {} with receipt: {}", associatedRefund.getId(), associatedRefund.getPapiReceiptNumber());
				});
			});
		} catch (Throwable e) {
			// always keep the status of refunds and dont let rollback.
			LOG.error(e.getMessage(), e);
			refundItem.setRefundState(RefundState.ERROR);
			refundItem.updated();
			refundItemDAO.save(refundItem);
		}
	}
    
    @Transactional
	public void refundPreparedItem(RefundItem refundItem) {
		try {
			String request = refundRequestBuilder.buildRequest(refundItem);
			String wsResponse = client.sendRequest(request, passwordBytes, REFUND_REQUEST_NS, request);
			RefundRequestResponse response = refundResponseParser.parseRequestResponse(wsResponse);
			if (isNotBlank(response.getErrorMessage())) {
				throw new ServiceException("Could not refund: " + refundItem.getId() + " with error: " + response.getErrorMessage());
			}
		} catch (Throwable e) {
			// always keep the status of refunds and dont let rollback.
			LOG.error(e.getMessage(), e);
			refundItem.setRefundState(RefundState.ERROR);
		}
		
		refundItem.updated();
		refundItemDAO.save(refundItem);
	}
}
