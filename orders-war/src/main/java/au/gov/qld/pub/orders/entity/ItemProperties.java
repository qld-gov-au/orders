package au.gov.qld.pub.orders.entity;

import java.util.Properties;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class ItemProperties {
    @Id
    private String productId;
    @Column
    private String productGroup;
    @Column
    private String title;
    @Column
    private String reference;
    @Column
    private String agency;
    @Column
    private String description;
    @Column
    private String disbursementId;
    @Column
    private String priceGst;
    @Column
    private String priceExGst;
    @Column
    private String costCenter;
    @Column
    private String glCode;
    @Column
    private String taxCode;
    @Column
    private String narrative;
    @Column
    private String notifyCustomerEmailField;
    @Column
    private String notifyBusinessEmail;
    @Column
    private String notifyCustomerEmailSubject;
    @Column
    private String notifyBusinessEmailSubject;
    @Column
    private String deliveryDetailsRequired;
    @Column
    private String customerDetailsRequired;
    @Column
    private String fields;
    @Column
    private String notifyBusinessFormUri;
    @Column
    private String notifyBusinessFormFilename;
    @Column
    private String notifyCustomerFormUri;
    @Column
    private String notifyCustomerFormFilename;
    @Column
    private String notifyCustomerFormDownloadTitle;

    public Item createItem() {
        return new Item(productId, productGroup, title, reference, agency,
                description, disbursementId, priceGst, priceExGst, costCenter,
                glCode, taxCode, narrative, notifyCustomerEmailField,
                notifyBusinessEmail, notifyBusinessEmailSubject,
                notifyCustomerEmailSubject, deliveryDetailsRequired,
                customerDetailsRequired, notifyBusinessFormUri,
                notifyBusinessFormFilename, notifyCustomerFormUri,
                notifyCustomerFormFilename, notifyCustomerFormDownloadTitle);
    }

    public String getProductId() {
        return productId;
    }

    public String getProductGroup() {
        return productGroup;
    }

    public String getTitle() {
        return title;
    }

    public String getReference() {
        return reference;
    }

    public String getAgency() {
        return agency;
    }

    public String getDescription() {
        return description;
    }

    public String getDisbursementId() {
        return disbursementId;
    }

    public String getPriceGst() {
        return priceGst;
    }

    public String getPriceExGst() {
        return priceExGst;
    }

    public String getCostCenter() {
        return costCenter;
    }

    public String getGlCode() {
        return glCode;
    }

    public String getTaxCode() {
        return taxCode;
    }

    public String getNarrative() {
        return narrative;
    }

    public String getNotifyCustomerEmailField() {
        return notifyCustomerEmailField;
    }

    public String getNotifyBusinessEmail() {
        return notifyBusinessEmail;
    }

    public String getNotifyCustomerEmailSubject() {
        return notifyCustomerEmailSubject;
    }

    public String getNotifyBusinessEmailSubject() {
        return notifyBusinessEmailSubject;
    }

    public String getDeliveryDetailsRequired() {
        return deliveryDetailsRequired;
    }

    public String getCustomerDetailsRequired() {
        return customerDetailsRequired;
    }

    public String getFields() {
        return fields;
    }

    public String getNotifyBusinessFormUri() {
        return notifyBusinessFormUri;
    }

    public String getNotifyBusinessFormFilename() {
        return notifyBusinessFormFilename;
    }

    public String getNotifyCustomerFormUri() {
        return notifyCustomerFormUri;
    }

    public String getNotifyCustomerFormFilename() {
        return notifyCustomerFormFilename;
    }

    public String getNotifyCustomerFormDownloadTitle() {
        return notifyCustomerFormDownloadTitle;
    }
    
    public static ItemProperties create(Properties properties) {
        if (properties == null) {
            return null;
        }
        
        ItemProperties itemProperties = new ItemProperties();
        itemProperties.productId = properties.getProperty("productId");
        itemProperties.productGroup = properties.getProperty("productGroup"); 
        itemProperties.title = properties.getProperty("title"); 
        itemProperties.fields = properties.getProperty("fields");
        itemProperties.reference = properties.getProperty("reference"); 
        itemProperties.agency = properties.getProperty("agency");
        itemProperties.description = properties.getProperty("description"); 
        itemProperties.disbursementId = properties.getProperty("disbursementId"); 
        itemProperties.priceGst = properties.getProperty("priceGst"); 
        itemProperties.priceExGst = properties.getProperty("priceExGst"); 
        itemProperties.costCenter = properties.getProperty("costCenter");
        itemProperties.glCode = properties.getProperty("glCode"); 
        itemProperties.taxCode = properties.getProperty("taxCode"); 
        itemProperties.narrative = properties.getProperty("narrative"); 
        itemProperties.notifyCustomerEmailField = properties.getProperty("notifyCustomerEmailField");
        itemProperties.notifyBusinessEmail = properties.getProperty("notifyBusinessEmail"); 
        itemProperties.notifyBusinessEmailSubject = properties.getProperty("notifyBusinessEmailSubject");
        itemProperties.notifyCustomerEmailSubject = properties.getProperty("notifyCustomerEmailSubject"); 
        itemProperties.deliveryDetailsRequired = properties.getProperty("deliveryDetailsRequired");
        itemProperties.customerDetailsRequired = properties.getProperty("customerDetailsRequired"); 
        itemProperties.notifyBusinessFormUri = properties.getProperty("notifyBusinessFormUri");
        itemProperties.notifyBusinessFormFilename = properties.getProperty("notifyBusinessFormFilename"); 
        itemProperties.notifyCustomerFormUri = properties.getProperty("notifyCustomerFormUri");
        itemProperties.notifyCustomerFormFilename = properties.getProperty("notifyCustomerFormFilename"); 
        itemProperties.notifyCustomerFormDownloadTitle = properties.getProperty("notifyCustomerFormDownloadTitle");
        return itemProperties;
    }
}
