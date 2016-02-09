package au.gov.qld.pub.orders.service.ws;

import java.util.HashMap;
import java.util.Map;

public class OrderDetails {
    
    private Map<String, String> orderlineQuantities = new HashMap<>();
    private Map<String, String> deliveryDetails = new HashMap<>();
    private Map<String, String> customerDetails = new HashMap<>();
    
    public Map<String, String> getDeliveryDetails() {
        return deliveryDetails;
    }
    
    public void setDeliveryDetails(Map<String, String> deliveryDetails) {
        this.deliveryDetails = deliveryDetails;
    }
    
    public Map<String, String> getCustomerDetails() {
        return customerDetails;
    }
    
    public void setCustomerDetails(Map<String, String> customerDetails) {
        this.customerDetails = customerDetails;
    }

    public Map<String, String> getOrderlineQuantities() {
        return orderlineQuantities;
    }

    public void setOrderlineQuantities(Map<String, String> orderlineQuantities) {
        this.orderlineQuantities = orderlineQuantities;
    }

    

}
