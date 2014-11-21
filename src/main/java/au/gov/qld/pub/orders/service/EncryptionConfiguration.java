package au.gov.qld.pub.orders.service;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jasypt.encryption.pbe.config.SimplePBEConfig;

public class EncryptionConfiguration extends SimplePBEConfig {
    private static final String ALGORITHM = "PBEWITHSHA256AND128BITAES-CBC-BC";
    private static final String KEY_FILENAME = ".orders.key";
    private static final String PASSWORD_LOC = System.getProperty("user.home") + File.separator + KEY_FILENAME; 
    
    public EncryptionConfiguration() throws IOException {
        super();
        String password = FileUtils.readFileToString(new File(PASSWORD_LOC)).trim();
        
        setProvider(new BouncyCastleProvider());
        setAlgorithm(ALGORITHM);
        setPassword(password);
    }
    
}
