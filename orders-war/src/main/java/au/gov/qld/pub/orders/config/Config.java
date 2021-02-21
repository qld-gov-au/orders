package au.gov.qld.pub.orders.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

@Configuration
public class Config {

    @Value("${sourceEncoding}")
    private String sourceEncoding;

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
