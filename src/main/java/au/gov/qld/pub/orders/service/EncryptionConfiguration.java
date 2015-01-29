package au.gov.qld.pub.orders.service;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jasypt.encryption.pbe.config.SimplePBEConfig;

public class EncryptionConfiguration extends SimplePBEConfig {
    private static final String ALGORITHM = "PBEWITHSHA256AND128BITAES-CBC-BC";
    
    public EncryptionConfiguration() throws IOException {
        super();
        Properties applicationProps = new Properties();
        applicationProps.load(EncryptionConfiguration.class.getClassLoader().getResourceAsStream("application.properties"));
        String password = FileUtils.readFileToString(new File(applicationProps.getProperty("keyfile"))).trim();
        
        setProvider(new BouncyCastleProvider());
        setAlgorithm(ALGORITHM);
        setPassword(password);
    }
    
}
