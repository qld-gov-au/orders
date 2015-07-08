package au.gov.qld.pub.orders.entity;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import au.gov.qld.pub.orders.dao.JsonHelper;

@Entity
public class Item {

    private static final List<String> VALID_NOTIFY_CUSTOMER_EMAIL_FIELDS = asList("deliveryDetails", "customerDetails");
    
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
    
    @SuppressWarnings("unused")
    private Item() {
        //for hibernate
    }
    
    public Item(String productId, String productGroup, String title, String reference,
            String agency, String description, String disbursementId,
            String priceGst, String priceExGst, String costCenter,
            String glCode, String taxCode, String narrative, String notifyCustomerEmailField,
            String notifyBusinessEmail, String notifyBusinessEmailSubject, String notifyCustomerEmailSubject,
            String deliveryDetailsRequired, String customerDetailsRequired,
            String notifyBusinessFormUri, String notifyBusinessFormFilename, String notifyCustomerFormUri, String notifyCustomerFormFilename, String notifyCustomerFormDownloadTitle) {
        this.id = UUID.randomUUID().toString();
        this.cartState = CartState.NEW;
        
        this.notifyCustomerEmailField = notifyCustomerEmailField;
        this.notifyBusinessEmail = notifyBusinessEmail;
        this.notifyBusinessEmailSubject = notifyBusinessEmailSubject;
        this.notifyCustomerEmailSubject = notifyCustomerEmailSubject;
        if (isNotBlank(notifyCustomerEmailField) && !VALID_NOTIFY_CUSTOMER_EMAIL_FIELDS.contains(notifyCustomerEmailField)) {
            throw new IllegalStateException("Invalid notifyCustomerEmailField of " 
                + notifyCustomerEmailField + " and should be in " + VALID_NOTIFY_CUSTOMER_EMAIL_FIELDS);
        }
        
        this.deliveryDetailsRequired = deliveryDetailsRequired;
        this.customerDetailsRequired = customerDetailsRequired;
        this.productId = productId;
        this.productGroup = productGroup;
        this.title = title;
        this.reference = reference;
        this.agency = agency;
        this.description = description;
        this.disbursementId = disbursementId;
        this.priceGst = priceGst;
        this.priceExGst = priceExGst;
        this.costCenter = costCenter;
        this.glCode = glCode;
        this.taxCode = taxCode;
        this.narrative = narrative;
        this.notifyBusinessFormUri = notifyBusinessFormUri;
        this.notifyBusinessFormFilename = notifyBusinessFormFilename;
        this.notifyCustomerFormUri = notifyCustomerFormUri;
        this.notifyCustomerFormFilename = notifyCustomerFormFilename;
        this.notifyCustomerFormDownloadTitle = notifyCustomerFormDownloadTitle;
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
        
    public void setFields(Map<String, String> fields) {
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
        return new EqualsBuilder().append(id, ((Item)obj).id).build();
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
}
