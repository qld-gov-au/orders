package au.gov.qld.pub.orders.entity;

import java.util.Date;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;

@Entity
public class RefundItem {
	@Id private String id;
	@Column private Date createdAt;
	@Column private Date lastModifiedAt;
	@Column private String reasonForRefund;
	@Column private int papiLineItemId;
	@Column private String orderLineId;
	@Column private String papiReceiptNumber;
	@Column private float amountToBeRefunded;
	
	@Column @Enumerated(EnumType.STRING)
    private RefundState refundState;
	@Column private String refundResponse;

	public RefundItem() {
		this.id = UUID.randomUUID().toString();
		this.createdAt = new Date();
		this.lastModifiedAt = createdAt;
	}
	
	public void updated() {
		this.lastModifiedAt = new Date();
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Date getLastModifiedAt() {
		return lastModifiedAt;
	}

	public void setLastModifiedAt(Date lastModifiedAt) {
		this.lastModifiedAt = lastModifiedAt;
	}

	public RefundState getRefundState() {
		return refundState;
	}

	public void setRefundState(RefundState refundState) {
		this.refundState = refundState;
	}

	public String getReasonForRefund() {
		return reasonForRefund;
	}

	public void setReasonForRefund(String reasonForRefund) {
		this.reasonForRefund = reasonForRefund;
	}

	public int getPapiLineItemId() {
		return papiLineItemId;
	}

	public void setPapiLineItemId(int papiLineItemId) {
		this.papiLineItemId = papiLineItemId;
	}

	public String getPapiReceiptNumber() {
		return papiReceiptNumber;
	}

	public void setPapiReceiptNumber(String papiReceiptNumber) {
		this.papiReceiptNumber = papiReceiptNumber;
	}

	public float getAmountToBeRefunded() {
		return amountToBeRefunded;
	}

	public void setAmountToBeRefunded(float amountToBeRefunded) {
		this.amountToBeRefunded = amountToBeRefunded;
	}

	public String getRefundResponse() {
		return refundResponse;
	}

	public void setRefundResponse(String refundResponse) {
		this.refundResponse = refundResponse;
	}

	public String getOrderLineId() {
		return orderLineId;
	}

	public void setOrderLineId(String orderLineId) {
		this.orderLineId = orderLineId;
	}
	
}
