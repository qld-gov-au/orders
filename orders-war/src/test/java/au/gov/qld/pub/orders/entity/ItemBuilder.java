package au.gov.qld.pub.orders.entity;

import au.gov.qld.pub.orders.service.ItemPropertiesDTO;



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
        ItemPropertiesDTO properties = new ItemPropertiesDTO();
        properties.setProductId(productId); 
        properties.setProductGroup(group); 
        properties.setTitle(title); 
        properties.setReference(reference); 
        properties.setAgency(agency); 
        properties.setDescription(description); 
        properties.setDisbursementId(disbursementId);
        properties.setPriceGst(priceGst);
        properties.setPriceExGst(priceExGst); 
        properties.setCostCenter(costCenter); 
        properties.setGlCode(glCode); 
        properties.setTaxCode(taxCode); 
        properties.setNarrative(narrative); 
        properties.setNotifyCustomerEmailField(notifyCustomerEmailField);
        properties.setNotifyBusinessEmail(notifyBusinessEmail); 
        properties.setNotifyBusinessEmailSubject(notifyBusinessEmailSubject); 
        properties.setNotifyCustomerEmailSubject(notifyCustomerEmailSubject); 
        properties.setDeliveryDetailsRequired(deliveryDetailsRequired); 
        properties.setCustomerDetailsRequired(customerDetailsRequired);
        properties.setNotifyBusinessFormUri(notifyBusinessFormUri); 
        properties.setNotifyBusinessFormFilename(notifyBusinessFormFilename); 
        properties.setNotifyCustomerFormUri(notifyCustomerFormUri); 
        properties.setNotifyCustomerFormFilename(notifyCustomerFormFilename); 
        properties.setNotifyCustomerFormDownloadTitle(notifyCustomerFormDownloadTitle);

        Item item = Item.createItem(properties);
        item.setQuantityPaid(quantityPaid);
        return item;
    }

    public ItemBuilder withPaid(String quantityPaid) {
        this.quantityPaid = quantityPaid; 
        return this;
    }
}
