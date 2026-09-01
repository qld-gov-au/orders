package au.gov.qld.pub.orders.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import au.gov.qld.pub.orders.service.PaymentInformation;

@Entity
public class NoticeToPay {
    @Id private String id;
    @Column(nullable = false) private String paymentInformationId;
    @Column(nullable = false) private String description;
    @Column(nullable = false) private long amount;
    @Column(nullable = false) private long amountGst;
    @Column private LocalDateTime notifiedAt;
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
        return obj instanceof NoticeToPay && id.equals(((NoticeToPay)obj).id);
    }

    public String getId() {
        return id;
    }

    public void setReceiptNumber(String receiptNumber) {
        this.receiptNumber = receiptNumber;
    }

    public void setNotifiedAt(LocalDateTime notifiedAt) {
        this.notifiedAt = notifiedAt;
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }

    public LocalDateTime getNotifiedAt() {
        return notifiedAt;
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
