package au.gov.qld.pub.orders.service;

import org.jasypt.encryption.pbe.config.SimplePBEConfig;

public class EncryptionConfiguration extends SimplePBEConfig {
    public EncryptionConfiguration() {
        super();
        setPassword("tK8diX83m6zBt6nX4Ym0Fl");
    }
    
}
