package au.gov.qld.pub.orders.service;

import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jasypt.encryption.pbe.config.SimplePBEConfig;

public class EncryptionConfiguration extends SimplePBEConfig {
    private static final String ALGORITHM = "PBEWITHSHA256AND128BITAES-CBC-BC";
    
    public EncryptionConfiguration() throws IOException {
        super();
        Properties applicationProps = new Properties();
        applicationProps.load(EncryptionConfiguration.class.getClassLoader().getResourceAsStream("application.properties"));
        
        setProvider(new BouncyCastleProvider());
        setAlgorithm(ALGORITHM);
        char[] password = IOUtils.toCharArray(new FileReader(applicationProps.getProperty("keyfile")));
        int end = password.length;
        for (int i=0; i < password.length; i++) {
            if (password[i] == '\n' || password[i] == '\r' || password[i] == ' ') {
                end = i;
                break;
            }
        }
        char[] trimmedPassword = Arrays.copyOfRange(password, 0, end);
        Arrays.fill(password, ' ');
        
        setProvider(new BouncyCastleProvider());
        setPasswordCharArray(trimmedPassword);
        Arrays.fill(trimmedPassword, ' '); 
    }
    
}
