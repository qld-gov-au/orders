package au.gov.qld.pub.orders.service;

import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class StartUp {
    private static final Logger LOG = LoggerFactory.getLogger(StartUp.class);
    
    public StartUp() {
        LOG.info("Adding bouncy castle provider");
    	Security.addProvider(new BouncyCastleProvider());
    }

}
