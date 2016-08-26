package au.gov.qld.pub.orders.dao;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Repository;

@Repository
public class FileItemPropertiesDAO {
    private static final Logger LOG = LoggerFactory.getLogger(FileItemPropertiesDAO.class);
    private static final Pattern VALID_PATH_PARAM = Pattern.compile("[a-zA-Z0-9_]");
    private static final Pattern PRODUCT_FILE_PATTERN = Pattern.compile("^(.+).product.properties$");

    private Properties find(String productId) {
        Matcher matcher = VALID_PATH_PARAM.matcher(productId);
        if (!matcher.find()) {
            LOG.error("Illegal productId: {}", productId);
            return null;
        }
        
        Properties properties = new Properties();
        try {
            load(productId + ".product.properties", properties);
            load(properties.getProperty("productGroup") + ".properties", properties);
            return properties;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return null;
        }
    }
    
    public Map<String, Properties> findProductProperties() throws IOException {
        Map<String, Properties> found = new HashMap<>();
        
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(getClass().getClassLoader());
        Resource[] resources = resolver.getResources("classpath:/products/properties/*.product.properties");
        for (Resource resource : resources) {
            Matcher matcher = PRODUCT_FILE_PATTERN.matcher(resource.getFilename());
            matcher.find();
            found.put(matcher.group(1), find(matcher.group(1)));
        }
        
        return found;
    }

    private void load(String productId, Properties properties) throws IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("products/properties/" + productId);
        if (is == null) {
            LOG.warn("Unknown productId: {}", productId);
            throw new IOException("Could not load properties");
        }
        
        try {
            properties.load(is);
        } finally {
            is.close();
        }
    }

}
