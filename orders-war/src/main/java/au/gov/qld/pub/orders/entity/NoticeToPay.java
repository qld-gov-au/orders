package au.gov.qld.pub.orders.entity;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import au.gov.qld.pub.orders.service.PaymentInformation;

@Entity
public class NoticeToPay {
    @Id private String id;
    @Column(nullable = false) private String paymentInformationId;
    @Column(nullable = false) private String description;
    @Column(nullable = false) private long amount;
    @Column(nullable = false) private long amountGst;
    @Column private Date notifiedAt;
    @Column private String receiptNumber;
    @Column private String exported;
    
    private NoticeToPay() {
        this.id = UUID.randomUUID().toString();
    }
    
    public NoticeToPay(String id, PaymentInformation paymentInformation) {
        this(paymentInformation);
        this.id = id;
    }
    
    public NoticeToPay(PaymentInformation paymentInformation) {
        this();
        this.paymentInformationId = paymentInformation.getReference();
        this.amount = paymentInformation.getAmountOwingInCents();
        this.amountGst = paymentInformation.getAmountOwingGstInCents();
        this.description = paymentInformation.getDescription();
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
    
    public String getDescription() {
        return description;
    }
    
    public long getAmount() {
        return amount;
    }
    
    public long getAmountGst() {
        return amountGst;
    }
    
    public String getExported() {
        return exported;
    }
}
