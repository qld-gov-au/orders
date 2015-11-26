package au.gov.qld.pub.orders.entity;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.LocalDateTime;

import au.gov.qld.pub.orders.dao.JsonHelper;
import au.gov.qld.pub.orders.service.ws.OrderDetails;

@Entity
@Table(name = "customer_order")
public class Order {
    @Id
    private final String id;
    
    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn
    private List<Item> items = new ArrayList<Item>();
    
    @Column
    private Date created;
    @Column
    private String customerDetails;
    @Column
    private String deliveryDetails;
    
    @Column
    private String receipt;
    @Column
    private String cartId;
    @Column
    private String generatedId;
    @Column
    private String paid;
    @Column
    private String notified;
    
    private Order() {
        this.id = UUID.randomUUID().toString();
    }
    
    public Order(String cartId) {
        this();
        this.cartId = cartId;
        this.created = new Date();
    }
    
    public String getCartId() {
        return cartId;
    }
    
    public Date getCreated() {
        return created;
    }
    
    public void setCartId(String cartId) {
        this.cartId = cartId;
    }

    public String getId() {
        return id;
    }

    public void setGeneratedId(String generatedId) {
        this.generatedId = generatedId;
    }

    public String getGeneratedId() {
        return generatedId;
    }

    public void add(Item item) {
        items.add(item);
    }

    public List<Item> getItems() {
        return items;
    }
    
    public String getPaid() {
        return paid;
    }
    
    public void setReceipt(String receipt) {
        this.receipt = receipt;
    }
    
    public void setPaid(String paid) {
        this.paid = paid;
    }

    public void setPaid(String receipt, OrderDetails orderDetails) {
        if (isBlank(receipt)) {
            throw new IllegalStateException();
        }
        
        this.receipt = receipt;
        this.paid = new LocalDateTime().toString();
        this.deliveryDetails = JsonHelper.serialise(orderDetails.getDeliveryDetails());
        this.customerDetails = JsonHelper.serialise(orderDetails.getCustomerDetails());
        for (Item item : items) {
            item.setQuantityPaid(orderDetails.getOrderlineQuantities().get(item.getId()));
        }
    }

    public String getReceipt() {
        return receipt;
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> getDeliveryDetailsMap() {
        return JsonHelper.deserialise(Map.class, deliveryDetails);
    }
    
    @SuppressWarnings("unchecked")
    public Map<String, String> getCustomerDetailsMap() {
        return JsonHelper.deserialise(Map.class, customerDetails);
    }
    
    public void setDeliveryDetailsMap(Map<String, String> map) {
        this.deliveryDetails = JsonHelper.serialise(map);
    }
    
    public void setCustomerDetailsMap(Map<String, String> map) {
        this.customerDetails = JsonHelper.serialise(map);
    }
    
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public void setNotified(String notified) {
        this.notified = notified;
    }
    
    public String getNotified() {
        return notified;
    }
    
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id).build();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Order && id.equals(((Order)obj).id);
    }
}
