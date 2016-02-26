package au.gov.qld.pub.orders.service;

import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.gov.qld.pub.orders.dao.FileItemPropertiesDAO;
import au.gov.qld.pub.orders.dao.ItemPropertiesDAO;

@Component
public class StartUp {
    private static final Logger LOG = LoggerFactory.getLogger(StartUp.class);
    
    @Autowired
    public StartUp(ItemPropertiesDAO itemPropertiesDAO, FileItemPropertiesDAO fileItemPropertiesDAO) {
        LOG.info("Adding bouncy castle provider");
    	Security.addProvider(new BouncyCastleProvider());
    }

}
