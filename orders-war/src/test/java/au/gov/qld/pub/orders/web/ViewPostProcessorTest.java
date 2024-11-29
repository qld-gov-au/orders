package au.gov.qld.pub.orders.web;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import au.gov.qld.pub.orders.service.ConfigurationService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ViewPostProcessorTest {
    static final String CONTEXT = "some context";
    static final String CART_ID = "some cart";
    static final String PAPI_BASE = "some papi base";
    static final String SERVICE_NAME = "some service name";
    static final String SERVICE_FRANCHISE = "some franchise";
    static final String SOURCE_URL = "some source url";

    ViewPostProcessor postProcessor;

    @Mock HttpServletRequest request;
    @Mock HttpServletResponse response;
    @Mock Object handler;
    @Mock ModelAndView mav;
    @Mock ModelMap modelMap;
    @Mock Cookie cartCookie;
    @Mock ConfigurationService configurationService;

    @BeforeEach
    public void setUp() {
        when(mav.getModelMap()).thenReturn(modelMap);
        when(cartCookie.getName()).thenReturn(Constants.CART_ID);
        when(cartCookie.getValue()).thenReturn(CART_ID);

        postProcessor = new ViewPostProcessor(configurationService);
        when(configurationService.getContext()).thenReturn(CONTEXT);
        when(configurationService.getPapiBase()).thenReturn(PAPI_BASE);
        when(configurationService.getServiceName()).thenReturn(SERVICE_NAME);
        when(configurationService.getServiceFranchise()).thenReturn(SERVICE_FRANCHISE);
        when(configurationService.getSourceUrl()).thenReturn(SOURCE_URL);
    }

    @Test
    public void addContext() throws Exception {
        postProcessor.postHandle(request, response, handler, mav);
        verify(modelMap).addAttribute("context", CONTEXT);
        verify(modelMap).addAttribute("papiBase", PAPI_BASE);
        verify(modelMap).addAttribute("serviceName", SERVICE_NAME);
        verify(modelMap).addAttribute("serviceFranchise", SERVICE_FRANCHISE);
        verify(modelMap).addAttribute("sourceUrl", SOURCE_URL);
    }

    @Test
    public void addCartId() throws Exception {
        when(request.getCookies()).thenReturn(new Cookie[] { cartCookie });
        postProcessor.postHandle(request, response, handler, mav);
        verify(modelMap).addAttribute(Constants.CART_ID, CART_ID);
    }
}
