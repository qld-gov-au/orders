package au.gov.qld.pub.orders.config;

import freemarker.template.*;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;

import java.io.IOException;

@org.springframework.context.annotation.Configuration
//ignoreResourceNotFound set to true so that Intellij builds don't fail
@PropertySource(value = {"classpath:git.properties"}, ignoreResourceNotFound = true)
public class FreemarkerConfig implements InitializingBean {

    @Value("${git.commit.id:notset}")
    private String commitId;

    @Value("${git.build.time:notset}")
    private String gitLastBuiltTime;

    @Value("${analytics.gtm-key:notset}")
    private String analyticsGtmKey;

    @Value("${analytics.mopinion:notset}")
    private String analyticsMopinion;

    @Value("${cdnEnvironment:static.qgov.net.au}")
    private String cdnEnvironment;

    @Autowired
    private Configuration configuration;

    @Override
    public void afterPropertiesSet() throws TemplateException {
        configuration.setSharedVariable("gitVersion", commitId);
        configuration.setSharedVariable("gitLastBuiltTime", gitLastBuiltTime);
        configuration.setSharedVariable("analyticsGtmKey", analyticsGtmKey);
        configuration.setSharedVariable("analyticsMopinion", analyticsMopinion);
        configuration.setSharedVariable("cdnEnvironment", cdnEnvironment);
    }

}
