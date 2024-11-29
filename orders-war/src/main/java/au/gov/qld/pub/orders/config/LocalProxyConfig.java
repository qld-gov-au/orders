package au.gov.qld.pub.orders.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.net.URI;

@Configuration()
public class LocalProxyConfig {

    @Value("${http_proxy:${HTTP_PROXY:}}")
    private String httpProxy;
    @Value("${https_proxy:${HTTPS_PROXY:}}")
    private String httpsProxy;
    @Value("${non_proxy_hosts:${NON_PROXY_HOSTS:}}")
    private String nonProxyHosts;

    private String localProxyHost;
    private int localProxyPort;

    @PostConstruct
    public void setSystemProxy() {
        System.setProperty("java.net.useSystemProxies", "true");

        if (!httpProxy.isEmpty()) {
            URI httpProxyUri = URI.create(httpProxy);
            String httpProxyHost = httpProxyUri.getHost();
            int httpProxyPort = httpProxyUri.getPort();
            if (httpProxyHost != null && httpProxyPort != -1) {
                System.setProperty("http.proxyHost", httpProxyHost);
                System.setProperty("http.proxyPort", Integer.toString(httpProxyPort, 10));
            }
        }

        if (!httpsProxy.isEmpty()) {
            URI httpsProxyUri = URI.create(httpProxy);
            String httpsProxyHost = httpsProxyUri.getHost();
            int httpsProxyPort = httpsProxyUri.getPort();
            if (httpsProxyHost != null && httpsProxyPort != -1) {
                System.setProperty("https.proxyHost", httpsProxyHost);
                System.setProperty("https.proxyPort", Integer.toString(httpsProxyPort));
                localProxyHost = httpsProxyHost;
                localProxyPort = httpsProxyPort;
            }
        }

        if (!nonProxyHosts.isEmpty()) {
            System.setProperty("http.nonProxyHosts", nonProxyHosts);
            System.setProperty("https.nonProxyHosts", nonProxyHosts);
        }
    }

    public String getLocalProxyHost() {
        return localProxyHost;
    }

    public int getLocalProxyPort() {
        return localProxyPort;
    }
}
