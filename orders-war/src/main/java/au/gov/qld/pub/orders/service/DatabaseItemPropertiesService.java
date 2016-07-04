package au.gov.qld.pub.orders.service;

import static org.apache.commons.lang3.StringUtils.defaultString;

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
        itemProperties.setProductId(properties.getProperty("productId"));
        itemProperties.setProductGroup(properties.getProperty("productGroup")); 
        itemProperties.setTitle(properties.getProperty("title")); 
        itemProperties.setFields(properties.getProperty("fields"));
        itemProperties.setReference(properties.getProperty("reference")); 
        itemProperties.setAgency(properties.getProperty("agency"));
        itemProperties.setDescription(properties.getProperty("description")); 
        itemProperties.setDisbursementId(properties.getProperty("disbursementId")); 
        itemProperties.setPriceGst(properties.getProperty("priceGst")); 
        itemProperties.setPriceExGst(properties.getProperty("priceExGst")); 
        itemProperties.setCostCenter(properties.getProperty("costCenter"));
        itemProperties.setGlCode(properties.getProperty("glCode")); 
        itemProperties.setTaxCode(properties.getProperty("taxCode")); 
        itemProperties.setNarrative(properties.getProperty("narrative")); 
        itemProperties.setNotifyCustomerEmailField(properties.getProperty("notifyCustomerEmailField"));
        itemProperties.setNotifyBusinessEmail(properties.getProperty("notifyBusinessEmail")); 
        itemProperties.setNotifyBusinessEmailSubject(properties.getProperty("notifyBusinessEmailSubject"));
        itemProperties.setNotifyCustomerEmailSubject(properties.getProperty("notifyCustomerEmailSubject")); 
        itemProperties.setDeliveryDetailsRequired(properties.getProperty("deliveryDetailsRequired"));
        itemProperties.setCustomerDetailsRequired(properties.getProperty("customerDetailsRequired")); 
        itemProperties.setNotifyBusinessFormUri(properties.getProperty("notifyBusinessFormUri"));
        itemProperties.setNotifyBusinessFormFilename(properties.getProperty("notifyBusinessFormFilename")); 
        itemProperties.setNotifyCustomerFormUri(properties.getProperty("notifyCustomerFormUri"));
        itemProperties.setNotifyCustomerFormFilename(properties.getProperty("notifyCustomerFormFilename")); 
        itemProperties.setNotifyCustomerFormDownloadTitle(properties.getProperty("notifyCustomerFormDownloadTitle"));
        itemProperties.setBundledDownload(Boolean.parseBoolean(defaultString(properties.getProperty("bundledDownload"), "false")));
        return itemProperties;
    }
}
