package au.gov.qld.pub.orders.config;


import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@EnableWebMvc
@EnableAsync
@SpringBootApplication
@ComponentScan(basePackages = {"au.gov.qld.pub.orders"})
@EnableCaching
@EnableScheduling
@EnableJpaRepositories(basePackages = "au.gov.qld.pub.orders.dao")
@EntityScan(basePackages = "au.gov.qld.pub.orders.entity")
@EnableConfigurationProperties
@EnableEncryptableProperties
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
