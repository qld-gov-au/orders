package au.gov.qld.pub.orders.entity;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import au.gov.qld.pub.orders.utils.DateTimeUtils;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import au.gov.qld.pub.orders.dao.JsonHelper;
import au.gov.qld.pub.orders.service.ws.OrderDetails;

@Entity
@Table(name = "customer_order")
public class Order {
    @Id private String id;
    
    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn
    private List<Item> items = new ArrayList<Item>();
    
    @Column
    private LocalDateTime created;
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
    private LocalDateTime paidAt;
    @Column
    private String notified;
    @Column
    private LocalDateTime notifiedAt;
    
    private Order() {
        this.id = UUID.randomUUID().toString();
    }
    
    public Order(String cartId) {
        this();
        this.cartId = cartId;
        this.created = LocalDateTime.now();
    }
    
    public String getCartId() {
        return cartId;
    }
    
    public LocalDateTime getCreated() {
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
    
    public LocalDateTime getPaidAt() {
		return paidAt;
	}

	public void setPaidAt(LocalDateTime paidAt) {
		this.paidAt = paidAt;
	}

	public LocalDateTime getNotifiedAt() {
		return notifiedAt;
	}

	public void setNotifiedAt(LocalDateTime notifiedAt) {
		this.notifiedAt = notifiedAt;
	}

	public void setPaid(String receipt, OrderDetails orderDetails) {
        if (isBlank(receipt)) {
            throw new IllegalStateException();
        }
        
        this.receipt = receipt;
        this.paidAt = LocalDateTime.now();
        this.paid = DateTimeUtils.DMY_DATE_FORMATTER.format(this.paidAt);
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

    public void setCreatedAt(LocalDateTime createdAt) {
        this.created = createdAt;
    }

	public List<Item> getPaidItems() {
		List<Item> paid = new ArrayList<>();
		for (Item item : items) {
			if (item.isPaid()) {
				paid.add(item);
			}
		}
		return paid;
	}

	public List<Item> getBundledPaidItems() {
		List<Item> result = new ArrayList<>();
		for (Item item : items) {
			if (item.isPaid() && item.isBundledDownload()) {
				result.add(item);
			}
		}
		return result;
	}
	
	public List<Item> getUnbundledPaidItems() {
		List<Item> result = new ArrayList<>();
		for (Item item : items) {
			if (item.isPaid() && !item.isBundledDownload()) {
				result.add(item);
			}
		}
		return result;
	}

	public void setId(String id) {
		this.id = id;
	}
}
