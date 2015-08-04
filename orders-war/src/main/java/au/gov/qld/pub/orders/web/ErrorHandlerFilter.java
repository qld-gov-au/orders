package au.gov.qld.pub.orders.web;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.support.WebApplicationContextUtils;

import au.gov.qld.pub.orders.service.ConfigurationService;

public class ErrorHandlerFilter implements Filter {
    private static final Logger LOG = LoggerFactory.getLogger(ErrorHandlerFilter.class);
    private ConfigurationService config;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain filterChain) throws IOException, ServletException {
        try {
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.sendRedirect(config.getErrorRedirect());
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        config = getConfigurationService(filterConfig);
    }

    @Override
    public void destroy() {
    }
    
    protected ConfigurationService getConfigurationService(FilterConfig filterConfig) {
        return WebApplicationContextUtils.getRequiredWebApplicationContext(
                filterConfig.getServletContext()).getBean(ConfigurationService.class);
    }
}