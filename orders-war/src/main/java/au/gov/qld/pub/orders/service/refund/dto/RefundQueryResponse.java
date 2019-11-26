package au.gov.qld.pub.orders.service.refund.dto;

import java.util.ArrayList;
import java.util.List;

import au.gov.qld.pub.orders.service.refund.dto.LineItem;

public class RefundQueryResponse {
	private List<LineItem> lineItem = new ArrayList<>();

	public List<LineItem> getLineItem() {
		return lineItem;
	}

	public void setLineItem(List<LineItem> lineItem) {
		this.lineItem = lineItem;
	}
	
}
