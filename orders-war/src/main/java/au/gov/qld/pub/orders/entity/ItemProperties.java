package au.gov.qld.pub.orders.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import au.gov.qld.pub.orders.service.ItemPropertiesDTO;

@Entity
public class ItemProperties extends ItemPropertiesDTO {
    @Id
    public String getProductId() {
        return productId;
    }
    @Column
    public String getProductGroup() {
        return productGroup;
    }
    @Column
    public String getTitle() {
        return title;
    }
    @Column
    public String getReference() {
        return reference;
    }
    @Column
    public String getAgency() {
        return agency;
    }
    @Column
    public String getDescription() {
        return description;
    }
    @Column
    public String getDisbursementId() {
        return disbursementId;
    }
    @Column
    public String getPriceGst() {
        return priceGst;
    }
    @Column
    public String getPriceExGst() {
        return priceExGst;
    }
    @Column
    public String getCostCenter() {
        return costCenter;
    }
    @Column
    public String getGlCode() {
        return glCode;
    }
    @Column
    public String getTaxCode() {
        return taxCode;
    }
    @Column
    public String getNarrative() {
        return narrative;
    }
    @Column
    public String getNotifyCustomerEmailField() {
        return notifyCustomerEmailField;
    }
    @Column
    public String getNotifyBusinessEmail() {
        return notifyBusinessEmail;
    }
    @Column
    public String getNotifyCustomerEmailSubject() {
        return notifyCustomerEmailSubject;
    }
    @Column
    public String getNotifyBusinessEmailSubject() {
        return notifyBusinessEmailSubject;
    }
    @Column
    public String getDeliveryDetailsRequired() {
        return deliveryDetailsRequired;
    }
    @Column
    public String getCustomerDetailsRequired() {
        return customerDetailsRequired;
    }
    @Column
    public String getFields() {
        return fields;
    }
    @Column
    public String getNotifyBusinessFormUri() {
        return notifyBusinessFormUri;
    }
    @Column
    public String getNotifyBusinessFormFilename() {
        return notifyBusinessFormFilename;
    }
    @Column
    public String getNotifyCustomerFormUri() {
        return notifyCustomerFormUri;
    }
    @Column
    public String getNotifyCustomerFormFilename() {
        return notifyCustomerFormFilename;
    }
    @Column
    public String getNotifyCustomerFormDownloadTitle() {
        return notifyCustomerFormDownloadTitle;
    }
    
    @Override
    public String toString() {
        return getProductId();
    }
}
