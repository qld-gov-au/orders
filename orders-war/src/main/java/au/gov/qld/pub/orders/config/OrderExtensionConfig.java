package au.gov.qld.pub.orders.config;

import au.gov.qld.pub.orders.dao.FileItemPropertiesDAO;
import au.gov.qld.pub.orders.dao.ItemPropertiesDAO;
import au.gov.qld.pub.orders.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrderExtensionConfig {

    // Extend with your own payment information service by overwriting this file in your overlay project
    @ConditionalOnMissingBean
    @Bean(name = "paymentInformationService")
    public StubPaymentInformationService paymentInformationService(){
        return new StubPaymentInformationService();
    }

    // Extend with your own payment information service by overwriting this file in your overlay project
    @Autowired
    @ConditionalOnMissingBean
    @Bean(name = "itemPropertiesService")
    public DatabaseItemPropertiesService databaseItemPropertiesService(ItemPropertiesDAO dao, FileItemPropertiesDAO fileItemPropertiesDAO){
        return new DatabaseItemPropertiesService(dao, fileItemPropertiesDAO);
    }

    //Extend with your own additional mail content service by overwriting this file in your overlay project
    @ConditionalOnMissingBean
    @Bean(name = "additionalMailContentService")
    public AdditionalMailContentService stubAdditionalMailContentService() {
        return new StubAdditionalMailContentService();
    }

    // Extend with your own additional notification service by overwriting this file in your overlay project
    @ConditionalOnMissingBean
    @Bean(name = "additionalNotificationService")
    public AdditionalNotificationService stubAdditionalNotificationService() {
        return new StubAdditionalNotificationService();
    }

}
