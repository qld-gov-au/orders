package au.gov.qld.pub.orders.service.refund.dto;

public class LineItem {
	private int papiLineItemId;
	private String orderLineId;
	private int quatity;
	private String agencyReference;
	
	public int getPapiLineItemId() {
		return papiLineItemId;
	}
	public void setPapiLineItemId(int papiLineItemId) {
		this.papiLineItemId = papiLineItemId;
	}
	public String getOrderLineId() {
		return orderLineId;
	}
	public void setOrderLineId(String orderLineId) {
		this.orderLineId = orderLineId;
	}
	public int getQuatity() {
		return quatity;
	}
	public void setQuatity(int quatity) {
		this.quatity = quatity;
	}
	public void setAgencyReference(String agencyReference) {
		this.agencyReference = agencyReference;
	}
	public String getAgencyReference() {
		return agencyReference;
	}
}
