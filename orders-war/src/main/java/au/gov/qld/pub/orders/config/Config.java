package au.gov.qld.pub.orders.config;

import au.gov.qld.pub.orders.service.EncryptionConfiguration;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.spring4.properties.EncryptablePropertyPlaceholderConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.IOException;



@Configuration
public class Config {
    @Autowired
    public Config(){

    }


    @Value("${sourceEncoding}")
    private String sourceEncoding;
    @Value("${keyfile}")
    private String keyfilename;
    @Value("${keyfilefallbackvalue}")
    private String keyfilefallbackvalue;

    @Bean
    public EncryptionConfiguration encryptionConfig() {
        try {
            return new EncryptionConfiguration(keyfilename, keyfilefallbackvalue);
        } catch (IOException e) {
            throw new RuntimeException("its dead jim", e);
        }
    }

    @Bean(name = "configurationEncryptor")
    public StandardPBEStringEncryptor configurationEncryptor() {
        StandardPBEStringEncryptor spbese = new StandardPBEStringEncryptor();
        spbese.setConfig(encryptionConfig());
        return spbese;
    }

    @Bean(name = "propertyConfigurer")
    public EncryptablePropertyPlaceholderConfigurer encryptablePropertyPlaceholderConfigurer() {
        EncryptablePropertyPlaceholderConfigurer e = new EncryptablePropertyPlaceholderConfigurer(configurationEncryptor());
        e.setLocation(new ClassPathResource("application.properties"));
        return e;
    }

    @Bean(name = "messageSource")
    public ReloadableResourceBundleMessageSource messageSource() {
        ReloadableResourceBundleMessageSource e = new ReloadableResourceBundleMessageSource();
        e.setBasename("classpath:messages");
        e.setDefaultEncoding(sourceEncoding);
        e.setCacheSeconds(10); //reload message every 10 seconds;
        return e;
    }

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("default");
    }

}
