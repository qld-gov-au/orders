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
    private final Map<String, Properties> fileProducts;
    private final FileItemPropertiesDAO fileItemPropertiesDAO;
	
    @Autowired
    public StartUp(ItemPropertiesDAO itemPropertiesDAO, FileItemPropertiesDAO fileItemPropertiesDAO) throws IOException {
        this.fileItemPropertiesDAO = fileItemPropertiesDAO;
        LOG.info("Adding bouncy castle provider");
    	Security.addProvider(new BouncyCastleProvider());
    	this.itemPropertiesDAO = itemPropertiesDAO;
    	this.fileProducts = findProductProperties();
    }

	private Map<String, Properties> findProductProperties() throws IOException {
	    Map<String, Properties> found = new HashMap<>();
	    
	    PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(getClass().getClassLoader());
	    Resource[] resources = resolver.getResources("classpath:/products/properties/*.product.properties");
	    for (Resource resource : resources) {
	        Matcher matcher = PRODUCT_FILE_PATTERN.matcher(resource.getFilename());
	        matcher.find();
	        found.put(matcher.group(1), fileItemPropertiesDAO.find(matcher.group(1)));
	    }
        return found;
    }

    @Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		LOG.info("Application event: {}", event.toString());
		for (Map.Entry<String, Properties> fileProduct : fileProducts.entrySet()) {
    		if (!itemPropertiesDAO.exists(fileProduct.getKey())) {
    		    LOG.info("Adding product: {} from properties", fileProduct.getKey());
    		    itemPropertiesDAO.save(ItemProperties.create(fileProduct.getValue()));
    		}
		}
	}

}
