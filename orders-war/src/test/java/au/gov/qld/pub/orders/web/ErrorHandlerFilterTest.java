package au.gov.qld.pub.orders.web;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import au.gov.qld.pub.orders.service.ConfigurationService;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ErrorHandlerFilterTest {
    private static final String RIDIRECT = "some redirect";

    @Mock ConfigurationService configurationService;
    @Mock FilterConfig filterConfig;
    @Mock HttpServletRequest request;
    @Mock HttpServletResponse response;
    @Mock FilterChain filterChain;

    ErrorHandlerFilter filter;

    @BeforeEach
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
        verifyNoInteractions(response);
    }
}
