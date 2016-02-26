package au.gov.qld.pub.orders.service;

import java.io.IOException;
import java.security.Security;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import au.gov.qld.pub.orders.dao.FileItemPropertiesDAO;
import au.gov.qld.pub.orders.dao.ItemPropertiesDAO;
import au.gov.qld.pub.orders.entity.ItemProperties;

@Component
public class StartUp implements ApplicationListener<ContextRefreshedEvent> {
	private static final Logger LOG = LoggerFactory.getLogger(StartUp.class);
    private static final Pattern PRODUCT_FILE_PATTERN = Pattern.compile("^(.+).product.properties$");
    private final ItemPropertiesDAO itemPropertiesDAO;
    private final FileItemPropertiesDAO fileItemPropertiesDAO;
	
    @Autowired
    public StartUp(ItemPropertiesDAO itemPropertiesDAO, FileItemPropertiesDAO fileItemPropertiesDAO) {
        this.fileItemPropertiesDAO = fileItemPropertiesDAO;
        LOG.info("Adding bouncy castle provider");
    	Security.addProvider(new BouncyCastleProvider());
    	this.itemPropertiesDAO = itemPropertiesDAO;
    }

	private Map<String, Properties> findProductProperties() {
	    Map<String, Properties> found = new HashMap<>();
	    
	    PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(getClass().getClassLoader());
        try {
            Resource[] resources = resolver.getResources("classpath:/products/properties/*.product.properties");
            for (Resource resource : resources) {
                Matcher matcher = PRODUCT_FILE_PATTERN.matcher(resource.getFilename());
                matcher.find();
                found.put(matcher.group(1), fileItemPropertiesDAO.find(matcher.group(1)));
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
	    
        return found;
    }

    @Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
        Map<String, Properties> fileProducts = findProductProperties();
		LOG.info("Application event: {}", event.toString());
		for (Map.Entry<String, Properties> fileProduct : fileProducts.entrySet()) {
    		if (!itemPropertiesDAO.exists(fileProduct.getKey())) {
    		    LOG.info("Adding product: {} from properties", fileProduct.getKey());
    		    itemPropertiesDAO.save(create(fileProduct.getValue()));
    		}
		}
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
