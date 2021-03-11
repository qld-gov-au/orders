package au.gov.qld.pub.orders.config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("PROXY")
public class LocalProxyConfig implements InitializingBean {

    @Value("${local.proxy.host}")
    private String localProxyHost;

    @Value("${local.proxy.port}")
    private Integer localProxyPort;

    @Override
    public void afterPropertiesSet() throws Exception {
        System.setProperty("http.proxyHost", localProxyHost);
        System.setProperty("http.proxyPort", String.valueOf(localProxyPort));
        System.setProperty("https.proxyHost", localProxyHost);
        System.setProperty("https.proxyPort", String.valueOf(localProxyPort));
    }

    public String getLocalProxyHost() {
        return localProxyHost;
    }

    public Integer getLocalProxyPort() {
        return localProxyPort;
    }
}
