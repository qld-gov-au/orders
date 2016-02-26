package au.gov.qld.pub.orders.service;

import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import au.gov.qld.pub.orders.dao.FileItemPropertiesDAO;
import au.gov.qld.pub.orders.dao.ItemPropertiesDAO;
import au.gov.qld.pub.orders.entity.ItemProperties;

public class DatabaseItemPropertiesService implements ItemPropertiesService, ApplicationListener<ContextRefreshedEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(DatabaseItemPropertiesService.class);
    
    private final ItemPropertiesDAO dao;
    private final FileItemPropertiesDAO fileItemPropertiesDAO;

    @Autowired
    public DatabaseItemPropertiesService(ItemPropertiesDAO dao, FileItemPropertiesDAO fileItemPropertiesDAO) {
        this.dao = dao;
        this.fileItemPropertiesDAO = fileItemPropertiesDAO;
    }
    
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        loadFileProperties();
    }
    
    private void loadFileProperties() {
        Map<String, Properties> fileProducts = fileItemPropertiesDAO.findProductProperties();
        for (Map.Entry<String, Properties> fileProduct : fileProducts.entrySet()) {
            if (!dao.exists(fileProduct.getKey())) {
                LOG.info("Adding product: {} from properties", fileProduct.getKey());
                dao.save(create(fileProduct.getValue()));
            }
        }
    }
    
    @Override
    public ItemProperties find(String productId) {
        return dao.findOne(productId);
    }
    
    private static ItemProperties create(Properties properties) {
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
