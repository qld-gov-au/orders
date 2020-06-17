package au.gov.qld.pub.orders.entity;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import au.gov.qld.pub.orders.dao.JsonHelper;
import au.gov.qld.pub.orders.service.ItemPropertiesDTO;
import au.gov.qld.pub.orders.service.NotifyType;

@Entity
public class Item {

    @Id
    private String id;
    @Column private String productId;
    @Column private String productGroup;
    @Column private String title;
    @Column private String reference;
    @Column private String agency;
    @Column private String description;
    @Column private String disbursementId;
    @Column private String priceGst;
    @Column private String priceExGst;
    @Column private String costCenter;
    @Column private String glCode;
    @Column private String taxCode;
    @Column private String narrative;
    @Column private String notifyCustomerEmailField;
    @Column private String notifyBusinessEmail;
    @Column private String notifyCustomerEmailSubject;
    @Column private String notifyBusinessEmailSubject;
    @Column private String deliveryDetailsRequired;
    @Column private String customerDetailsRequired;
    @Column private String quantityPaid;
    @Column private String fields;
    @Column private String notifyBusinessFormUri;
    @Column private String notifyBusinessFormFilename;
    @Column private String notifyCustomerFormUri;
    @Column private String notifyCustomerFormFilename;
    @Column private String notifyCustomerFormDownloadTitle;
    
    @Column @Enumerated(EnumType.STRING)
    private CartState cartState;
    @Column private boolean bundledDownload;
    
    protected Item() {
        this.id = UUID.randomUUID().toString();
        this.cartState = CartState.NEW;
    }
    
    public static Item createItem(ItemPropertiesDTO dto) {
        Item item = new Item();
        try {
            BeanUtils.copyProperties(item, dto);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return item;
    }

    public String getId() {
        return id;
    }

    public String getProductId() {
        return productId;
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

    public CartState getCartState() {
        return cartState;
    }
    
    public boolean isNew() {
        return CartState.NEW == cartState;
    }

    public void setCartState(CartState cartState) {
        if (!this.cartState.canUpgrade(cartState)) {
            throw new IllegalStateException("Cannot move from: " + this.cartState + " to " + cartState);
        }
        this.cartState = cartState;
    }

    public void setQuantityPaid(String value) {
        this.quantityPaid = value;
        if (isBlank(value)) {
            this.quantityPaid = "0";
        }
    }
    
    public boolean isPaid() {
        return Integer.parseInt(defaultString(this.quantityPaid, "0")) > 0;
    }
    
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public String getQuantityPaid() {
        return quantityPaid;
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> getFieldsMap() {
        Map<String, String> map = JsonHelper.deserialise(Map.class, fields);
        return map != null ? map : new TreeMap<String, String>();
    }

    public void setFields(String fields) {
        this.fields = fields;
    }

    public void setFieldsFromMap(Map<String, String> fields) {
        this.fields = JsonHelper.serialise(fields);
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

    public Set<String> getCustomerDetailsRequiredSet() {
        return new HashSet<String>(asList(defaultString(customerDetailsRequired).split(",")));
    }

    public Set<String> getDeliveryDetailsRequiredSet() {
        return new HashSet<String>(asList(defaultString(deliveryDetailsRequired).split(",")));
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

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id).build();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Item && id.equals(((Item)obj).id);
    }

    public String getProductGroup() {
        return productGroup;
    }

    public void setProductGroup(String group) {
        this.productGroup = group;
    }

    public String getNotifyBusinessFormUri() {
        return notifyBusinessFormUri;
    }
    
    public String getNotifyFormUri(NotifyType type) {
    	return NotifyType.BUSINESS.equals(type) ? getNotifyBusinessFormUri() : getNotifyCustomerFormUri();
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

	public boolean isBundledDownload() {
		return bundledDownload;
	}
	
	public void setBundledDownload(boolean bundledDownload) {
		this.bundledDownload = bundledDownload;
	}

    public void setId(String id) {
        this.id = id;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public void setAgency(String agency) {
        this.agency = agency;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDisbursementId(String disbursementId) {
        this.disbursementId = disbursementId;
    }

    public void setPriceGst(String priceGst) {
        this.priceGst = priceGst;
    }

    public void setPriceExGst(String priceExGst) {
        this.priceExGst = priceExGst;
    }

    public void setCostCenter(String costCenter) {
        this.costCenter = costCenter;
    }

    public void setGlCode(String glCode) {
        this.glCode = glCode;
    }

    public void setTaxCode(String taxCode) {
        this.taxCode = taxCode;
    }

    public void setNarrative(String narrative) {
        this.narrative = narrative;
    }

    public void setNotifyCustomerEmailField(String notifyCustomerEmailField) {
        this.notifyCustomerEmailField = notifyCustomerEmailField;
    }

    public void setNotifyBusinessEmail(String notifyBusinessEmail) {
        this.notifyBusinessEmail = notifyBusinessEmail;
    }

    public void setNotifyCustomerEmailSubject(String notifyCustomerEmailSubject) {
        this.notifyCustomerEmailSubject = notifyCustomerEmailSubject;
    }

    public void setNotifyBusinessEmailSubject(String notifyBusinessEmailSubject) {
        this.notifyBusinessEmailSubject = notifyBusinessEmailSubject;
    }

    public void setNotifyBusinessFormUri(String notifyBusinessFormUri) {
        this.notifyBusinessFormUri = notifyBusinessFormUri;
    }

    public void setNotifyBusinessFormFilename(String notifyBusinessFormFilename) {
        this.notifyBusinessFormFilename = notifyBusinessFormFilename;
    }

    public void setNotifyCustomerFormUri(String notifyCustomerFormUri) {
        this.notifyCustomerFormUri = notifyCustomerFormUri;
    }

    public void setNotifyCustomerFormFilename(String notifyCustomerFormFilename) {
        this.notifyCustomerFormFilename = notifyCustomerFormFilename;
    }

    public void setNotifyCustomerFormDownloadTitle(String notifyCustomerFormDownloadTitle) {
        this.notifyCustomerFormDownloadTitle = notifyCustomerFormDownloadTitle;
    }
}
