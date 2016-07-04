package au.gov.qld.pub.orders.service;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.List;


public class ItemPropertiesDTO {
    private static final List<String> VALID_NOTIFY_CUSTOMER_EMAIL_FIELDS = asList("deliveryDetails", "customerDetails");
    
    protected String productId;
    protected String productGroup;
    protected String title;
    protected String reference;
    protected String agency;
    protected String description;
    protected String disbursementId;
    protected String priceGst;
    protected String priceExGst;
    protected String costCenter;
    protected String glCode;
    protected String taxCode;
    protected String narrative;
    protected String notifyCustomerEmailField;
    protected String notifyBusinessEmail;
    protected String notifyCustomerEmailSubject;
    protected String notifyBusinessEmailSubject;
    protected String deliveryDetailsRequired;
    protected String customerDetailsRequired;
    protected String fields;
    protected String notifyBusinessFormUri;
    protected String notifyBusinessFormFilename;
    protected String notifyCustomerFormUri;
    protected String notifyCustomerFormFilename;
    protected String notifyCustomerFormDownloadTitle;
    protected boolean bundledDownload;
    
    public String getProductId() {
        return productId;
    }
    public void setProductId(String productId) {
        this.productId = productId;
    }
    public String getProductGroup() {
        return productGroup;
    }
    public void setProductGroup(String productGroup) {
        this.productGroup = productGroup;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getReference() {
        return reference;
    }
    public void setReference(String reference) {
        this.reference = reference;
    }
    public String getAgency() {
        return agency;
    }
    public void setAgency(String agency) {
        this.agency = agency;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getDisbursementId() {
        return disbursementId;
    }
    public void setDisbursementId(String disbursementId) {
        this.disbursementId = disbursementId;
    }
    public String getPriceGst() {
        return priceGst;
    }
    public void setPriceGst(String priceGst) {
        this.priceGst = priceGst;
    }
    public String getPriceExGst() {
        return priceExGst;
    }
    public void setPriceExGst(String priceExGst) {
        this.priceExGst = priceExGst;
    }
    public String getCostCenter() {
        return costCenter;
    }
    public void setCostCenter(String costCenter) {
        this.costCenter = costCenter;
    }
    public String getGlCode() {
        return glCode;
    }
    public void setGlCode(String glCode) {
        this.glCode = glCode;
    }
    public String getTaxCode() {
        return taxCode;
    }
    public void setTaxCode(String taxCode) {
        this.taxCode = taxCode;
    }
    public String getNarrative() {
        return narrative;
    }
    public void setNarrative(String narrative) {
        this.narrative = narrative;
    }
    public String getNotifyCustomerEmailField() {
        return notifyCustomerEmailField;
    }
    public void setNotifyCustomerEmailField(String notifyCustomerEmailField) {
        if (isNotBlank(notifyCustomerEmailField) && !VALID_NOTIFY_CUSTOMER_EMAIL_FIELDS.contains(notifyCustomerEmailField)) {
            throw new IllegalStateException("Invalid notifyCustomerEmailField of " 
                + notifyCustomerEmailField + " and should be in " + VALID_NOTIFY_CUSTOMER_EMAIL_FIELDS);
        }
        this.notifyCustomerEmailField = notifyCustomerEmailField;
    }
    public String getNotifyBusinessEmail() {
        return notifyBusinessEmail;
    }
    public void setNotifyBusinessEmail(String notifyBusinessEmail) {
        this.notifyBusinessEmail = notifyBusinessEmail;
    }
    public String getNotifyCustomerEmailSubject() {
        return notifyCustomerEmailSubject;
    }
    public void setNotifyCustomerEmailSubject(String notifyCustomerEmailSubject) {
        this.notifyCustomerEmailSubject = notifyCustomerEmailSubject;
    }
    public String getNotifyBusinessEmailSubject() {
        return notifyBusinessEmailSubject;
    }
    public void setNotifyBusinessEmailSubject(String notifyBusinessEmailSubject) {
        this.notifyBusinessEmailSubject = notifyBusinessEmailSubject;
    }
    public String getDeliveryDetailsRequired() {
        return deliveryDetailsRequired;
    }
    public void setDeliveryDetailsRequired(String deliveryDetailsRequired) {
        this.deliveryDetailsRequired = deliveryDetailsRequired;
    }
    public String getCustomerDetailsRequired() {
        return customerDetailsRequired;
    }
    public void setCustomerDetailsRequired(String customerDetailsRequired) {
        this.customerDetailsRequired = customerDetailsRequired;
    }
    public String getFields() {
        return fields;
    }
    public void setFields(String fields) {
        this.fields = fields;
    }
    public String getNotifyBusinessFormUri() {
        return notifyBusinessFormUri;
    }
    public void setNotifyBusinessFormUri(String notifyBusinessFormUri) {
        this.notifyBusinessFormUri = notifyBusinessFormUri;
    }
    public String getNotifyBusinessFormFilename() {
        return notifyBusinessFormFilename;
    }
    public void setNotifyBusinessFormFilename(String notifyBusinessFormFilename) {
        this.notifyBusinessFormFilename = notifyBusinessFormFilename;
    }
    public String getNotifyCustomerFormUri() {
        return notifyCustomerFormUri;
    }
    public void setNotifyCustomerFormUri(String notifyCustomerFormUri) {
        this.notifyCustomerFormUri = notifyCustomerFormUri;
    }
    public String getNotifyCustomerFormFilename() {
        return notifyCustomerFormFilename;
    }
    public void setNotifyCustomerFormFilename(String notifyCustomerFormFilename) {
        this.notifyCustomerFormFilename = notifyCustomerFormFilename;
    }
    public String getNotifyCustomerFormDownloadTitle() {
        return notifyCustomerFormDownloadTitle;
    }
    public void setNotifyCustomerFormDownloadTitle(String notifyCustomerFormDownloadTitle) {
        this.notifyCustomerFormDownloadTitle = notifyCustomerFormDownloadTitle;
    }
	public void setBundledDownload(boolean bundledDownload) {
		this.bundledDownload = bundledDownload;
	}
	public boolean isBundledDownload() {
		return bundledDownload;				
	}
}
