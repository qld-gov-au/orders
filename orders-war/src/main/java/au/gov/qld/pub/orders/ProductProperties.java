package au.gov.qld.pub.orders;

import java.util.Properties;

import au.gov.qld.pub.orders.entity.Item;

public class ProductProperties {
    public static final Item populate(Properties properties) {
        return new Item(properties.getProperty("productId"), properties.getProperty("productGroup"), properties.getProperty("title"), properties.getProperty("reference"),
                properties.getProperty("agency"), properties.getProperty("description"), properties.getProperty("disbursementId"),
                properties.getProperty("priceGst"), properties.getProperty("priceExGst"), properties.getProperty("costCenter"),
                properties.getProperty("glCode"), properties.getProperty("taxCode"), properties.getProperty("narrative"),
                properties.getProperty("notifyCustomerEmailField"), properties.getProperty("notifyBusinessEmail"), 
                properties.getProperty("notifyBusinessEmailSubject"), properties.getProperty("notifyCustomerEmailSubject"),
                properties.getProperty("deliveryDetailsRequired"), properties.getProperty("customerDetailsRequired"),
                properties.getProperty("notifyBusinessFormUri"), properties.getProperty("notifyBusinessFormFilename"),
                properties.getProperty("notifyCustomerFormUri"), properties.getProperty("notifyCustomerFormFilename"),
                properties.getProperty("notifyCustomerFormDownloadTitle"));
    }
}
