package au.gov.qld.pub.orders.web;

import static org.apache.commons.lang3.StringUtils.repeat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.view.RedirectView;

import au.gov.qld.pub.orders.service.ConfigurationService;
import au.gov.qld.pub.orders.service.NoticeToPayService;
import au.gov.qld.pub.orders.service.ServiceException;

@RunWith(MockitoJUnitRunner.class)
public class NoticeToPayControllerTest {
    private static final String DEFAULT_REDIRECT = "default redirect";
    private static final String SOURCE_PATTERN = "[a-z]";
    private static final String ID_PATTERN = "[0-9]";
    private static final String ID = "1";
    private static final String SOURCE = "t";
    private static final String NOTICE_TO_PAY = "some notice to pay";
    private static final String NOTICE_TO_PAY_ID = "some notice to pay id";    

    NoticeToPayController controller;
    @Mock ConfigurationService config;
    @Mock NoticeToPayService service;
    
    @Before
    public void setUp() throws ServiceException {
        when(config.getNoticeToPayDefaultRedirect()).thenReturn(DEFAULT_REDIRECT);
        when(config.getNoticeToPaySourcePattern()).thenReturn(SOURCE_PATTERN);
        when(config.getNoticeToPayIdPattern()).thenReturn(ID_PATTERN);
        when(service.create(ID, SOURCE)).thenReturn(NOTICE_TO_PAY);
        controller = new NoticeToPayController(config, service);
    }
    
    @Test
    public void redirectToDefaultWhenInvalidSource() throws ServiceException {
        RedirectView redirect = controller.payInFull(ID, "bogus");
        assertThat(redirect.getUrl(), is(DEFAULT_REDIRECT));
        assertThat(redirect.isExposePathVariables(), is(false));
    }
    
    @Test
    public void redirectToDefaultWhenBlankSource() throws ServiceException {
        RedirectView redirect = controller.payInFull(ID, null);
        assertThat(redirect.getUrl(), is(DEFAULT_REDIRECT));
        assertThat(redirect.isExposePathVariables(), is(false));
    }
    
    @Test
    public void redirectToSourceWhenInvalidId() throws ServiceException {
        RedirectView redirect = controller.payInFull("bogus", SOURCE);
        assertThat(redirect.getUrl(), is(SOURCE));
        assertThat(redirect.isExposePathVariables(), is(false));
    }
    
    @Test
    public void redirectToSourceWhenExceptionFromService() throws ServiceException {
        when(service.create(ID, SOURCE)).thenThrow(new ServiceException("expected"));
        RedirectView redirect = controller.payInFull(ID, SOURCE);
        assertThat(redirect.getUrl(), is(SOURCE));
        assertThat(redirect.isExposePathVariables(), is(false));
    }
    
    @Test
    public void redirectToSourceWhenBlankId() throws ServiceException {
        RedirectView redirect = controller.payInFull(null, SOURCE);
        assertThat(redirect.getUrl(), is(SOURCE));
        assertThat(redirect.isExposePathVariables(), is(false));
    }
    
    @Test
    public void redirectToNoticeToPay() throws ServiceException {
        RedirectView redirect = controller.payInFull(ID, SOURCE);
        assertThat(redirect.getUrl(), is(NOTICE_TO_PAY));
        assertThat(redirect.isExposePathVariables(), is(false));
    }
    
    @Test
    public void notifyById() throws ServiceException {
        ResponseEntity<String> result = controller.notifyPayment(NOTICE_TO_PAY_ID);
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
        verify(service).notifyPayment(NOTICE_TO_PAY_ID);
    }
    
    @Test
    public void throwExceptionIfNotifyByIdInvalidFormat() throws ServiceException {
        try {
            controller.notifyPayment(null);
            fail();
        } catch (ServiceException e) {
            verifyZeroInteractions(service);
        }
        
        try {
            controller.notifyPayment("");
            fail();
        } catch (ServiceException e) {
            verifyZeroInteractions(service);
        }
        
        try {
            controller.notifyPayment(repeat("a", 100));
            fail();
        } catch (ServiceException e) {
            verifyZeroInteractions(service);
        }
    }
}
