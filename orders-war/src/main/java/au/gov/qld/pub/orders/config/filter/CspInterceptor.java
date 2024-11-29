package au.gov.qld.pub.orders.config.filter;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Properties;
import java.util.UUID;

/**
 * Implements Content Security Policy version 3 headers with nonce as described here: https://www.w3.org/TR/CSP3/#strict-dynamic-usage
 */
@Component
public class CspInterceptor implements HandlerInterceptor {

    public static final String CSP_HEADER = "Content-Security-Policy";
    public static final String CSP_REPORT_ONLY_HEADER = "Content-Security-Policy-Report-Only";
    public static final String CSP_REPORT_TO_HEADER = "Report-To";
    public static final String CSP_NEL_HEADER = "NEL";

    public static final String CSP_NONCE_ATTRIB = "__csp_nonce";

    protected final PropertyPlaceholderHelper propertyPlaceholderHelper;

    private final boolean cspEnforce;
    private final String cspFilter;
    private final String cspReportTo;
    private final String cspNel;

    private String nonce;

    @Autowired
    public CspInterceptor(@Value("${reportUri.csp.enforce}") boolean cspEnforce,
                          @Value("${reportUri.csp.filter}") String cspFilter,
                          @Value("${reportUri.reportto}") String cspReportTo,
                          @Value("${reportUri.nel}") String cspNel) {
        this.cspEnforce = cspEnforce;
        this.cspFilter = cspFilter;
        this.cspReportTo = cspReportTo;
        this.cspNel = cspNel;

        propertyPlaceholderHelper = new PropertyPlaceholderHelper("[", "]");
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        nonce = generateNonce();
        String cspFilterWithInjectedNonce = populateCspFilterWithNonce(cspFilter, CSP_NONCE_ATTRIB, nonce);
        response.setHeader(cspEnforce ? CSP_HEADER : CSP_REPORT_ONLY_HEADER, cspFilterWithInjectedNonce);
        response.setHeader(CSP_REPORT_TO_HEADER, cspReportTo);
        response.setHeader(CSP_NEL_HEADER, cspNel);
        request.setAttribute(CSP_NONCE_ATTRIB, nonce);

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws
        Exception {
        // not implemented
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws
        Exception {
        // not implemented
    }

    protected String populateCspFilterWithNonce(String cspHeaderTemplate, String noncePlaceholder, String nonce) {
        Properties cspNonceProps = new Properties();
        cspNonceProps.setProperty(noncePlaceholder, nonce);

        return propertyPlaceholderHelper.replacePlaceholders(cspHeaderTemplate, cspNonceProps);
    }

    protected String generateNonce() {
        return UUID.randomUUID().toString();
    }

}
