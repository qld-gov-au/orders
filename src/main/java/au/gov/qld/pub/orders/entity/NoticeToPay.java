package au.gov.qld.pub.orders.entity;

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
	//TODO: save payment notification/s 
	
    private NoticeToPay() {
    	this.id = UUID.randomUUID().toString();
    }
    
    public NoticeToPay(String paymentInformationId) {
    	this();
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
}
