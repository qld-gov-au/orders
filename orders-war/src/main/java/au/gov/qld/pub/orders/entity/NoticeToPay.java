package au.gov.qld.pub.orders.entity;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Entity
public class NoticeToPay {
	@Id private String id;
    @Column private String paymentInformationId;
    @Column private Date notifiedAt;
    @Column private String receiptNumber;
	
    private NoticeToPay() {
    	this.id = UUID.randomUUID().toString();
    }
    
    public NoticeToPay(String paymentInformationId) {
    	this();
    	this.paymentInformationId = paymentInformationId;
    }
    
    public NoticeToPay(String id, String paymentInformationId) {
        this.id = id;
        this.paymentInformationId = paymentInformationId;
    }
    
	public String getPaymentInformationId() {
		return paymentInformationId;
	}
	
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id).build();
    }

    @Override
    public boolean equals(Object obj) {
        return new EqualsBuilder().append(id, ((NoticeToPay)obj).id).build();
    }

	public String getId() {
		return id;
	}

    public void setReceiptNumber(String receiptNumber) {
        this.receiptNumber = receiptNumber;
    }

    public void setNotifiedAt(Date notifiedAt) {
        this.notifiedAt = notifiedAt != null ? new Date(notifiedAt.getTime()) : null;
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }

    public Date getNotifiedAt() {
        return notifiedAt != null ? new Date(notifiedAt.getTime()) : null;
    }
}
