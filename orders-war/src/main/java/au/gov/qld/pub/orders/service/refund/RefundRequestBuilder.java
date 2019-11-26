package au.gov.qld.pub.orders.service.refund;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Component;

import au.gov.qld.pub.orders.entity.RefundItem;

@Component
public class RefundRequestBuilder {
	private final String QUERY_TEMPLATE = "<RefundQueryRequest><papiReceiptNumber>%s</papiReceiptNumber></RefundQueryRequest>";
	
	public String buildQuery(RefundItem refundItem) {
		return String.format(QUERY_TEMPLATE, refundItem.getPapiReceiptNumber());
	}

	public String buildRequest(RefundItem refundItem) {
		throw new NotImplementedException("TODO");
	}

}
