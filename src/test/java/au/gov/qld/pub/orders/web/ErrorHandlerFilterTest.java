package au.gov.qld.pub.orders.web;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import au.gov.qld.pub.orders.service.ConfigurationService;

@RunWith(MockitoJUnitRunner.class)
public class ErrorHandlerFilterTest {
    private static final String RIDIRECT = "some redirect";
    
    @Mock ConfigurationService configurationService;
    @Mock FilterConfig filterConfig;
    @Mock HttpServletRequest request;
    @Mock HttpServletResponse response;
    @Mock FilterChain filterChain;

    ErrorHandlerFilter filter;
    
    @Before
    public void setUp() throws Exception {
        when(configurationService.getErrorRedirect()).thenReturn(RIDIRECT);
        filter = new ErrorHandlerFilter() {
            @Override
            protected ConfigurationService getConfigurationService(FilterConfig filterConfig) {
                return configurationService;
            }
        };
        filter.init(filterConfig);
    }
    
    @Test
    public void redirectToErrorOnException() throws Exception {
        doThrow(new RuntimeException("expected")).when(filterChain).doFilter(request, response);
        
        filter.doFilter(request, response, filterChain);
        filter.destroy();
        verify(response).sendRedirect(RIDIRECT);
    }
    
    @Test
    public void dontRedirectWhenNoException() throws Exception {
        filter.doFilter(request, response, filterChain);
        filter.destroy();
        
        verify(filterChain).doFilter(request, response);
        verifyZeroInteractions(response);
    }
}
