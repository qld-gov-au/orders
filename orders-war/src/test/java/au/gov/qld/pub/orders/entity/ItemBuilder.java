package au.gov.qld.pub.orders.entity;



public class ItemBuilder {
    private String productId = "test";
    private String group;
    private String title;
    private String reference;
    private String agency;
    private String description;
    private String disbursementId;
    private String priceGst;
    private String priceExGst;
    private String costCenter;
    private String glCode;
    private String taxCode;
    private String narrative;
    private String notifyCustomerEmailField;
    private String notifyBusinessEmail;
    private String notifyBusinessEmailSubject;
    private String notifyCustomerEmailSubject;
    private String deliveryDetailsRequired;
    private String customerDetailsRequired;
    private String notifyBusinessFormUri;
    private String notifyBusinessFormFilename;
    private String notifyCustomerFormUri;
    private String notifyCustomerFormFilename;
    private String notifyCustomerFormDownloadTitle;
    private String quantityPaid = "0";

    public ItemBuilder withProductId(String productId) {
        this.productId = productId;
        return this;
    }
    
    public ItemBuilder withGroup(String group) {
        this.group = group;
        return this;
    }
    
    public ItemBuilder withNotifyCustomerEmailField(String notifyCustomerEmailField) {
        this.notifyCustomerEmailField = notifyCustomerEmailField;
        return this;
    }
    
    public Item build() {
        Item item = new Item(productId, group, title, reference, agency, description, disbursementId,
                priceGst, priceExGst, costCenter, glCode, taxCode, narrative, notifyCustomerEmailField,
                notifyBusinessEmail, notifyBusinessEmailSubject, notifyCustomerEmailSubject, deliveryDetailsRequired, customerDetailsRequired,
                notifyBusinessFormUri, notifyBusinessFormFilename, notifyCustomerFormUri, notifyCustomerFormFilename, notifyCustomerFormDownloadTitle);
        item.setQuantityPaid(quantityPaid);
        return item;
    }

    public ItemBuilder withPaid(String quantityPaid) {
        this.quantityPaid = quantityPaid; 
        return this;
    }
}
