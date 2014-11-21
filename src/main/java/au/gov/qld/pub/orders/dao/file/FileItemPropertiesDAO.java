package au.gov.qld.pub.orders.dao.file;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import au.gov.qld.pub.orders.dao.ItemPropertiesDAO;

@Repository
public class FileItemPropertiesDAO implements ItemPropertiesDAO {
    private static final Logger LOG = LoggerFactory.getLogger(FileItemPropertiesDAO.class);
    private static final Pattern VALID_PATH_PARAM = Pattern.compile("[a-zA-Z0-9_]");

    @Override
    public Properties find(String productId) {
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
