package au.gov.qld.pub.orders.service;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.HashSet;

import org.joda.time.DateTimeUtils;
import org.joda.time.LocalDateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ScheduleServiceTest {
    private static final String UNPAID_ORDER_ID = "some unpaid order id";
    private static final int MAX_AGE = 10;

    @Mock ConfigurationService config;
    @Mock OrderService orderService;
    @Mock NotifyService notifyService;
    
    Date minAge;
    ScheduleService service;

    @Before
    public void setUp() throws ServiceException {
        DateTimeUtils.setCurrentMillisFixed(new Date().getTime());
        minAge = new LocalDateTime().minusMillis(MAX_AGE).toDate();
        when(config.getMaxAgeForRetry()).thenReturn(MAX_AGE);
        service = new ScheduleService(config, orderService, notifyService);
    }
    
    @After
    public void tearDown() {
        DateTimeUtils.setCurrentMillisSystem();
    }
    
    @Test
    public void statusCheckerChecksUnpaidAndUnnotifiedOrdersAndFulfills() throws ServiceException {
        when(orderService.findUnpaidOrderIds(minAge)).thenReturn(new HashSet<>(asList(UNPAID_ORDER_ID)));
        when(orderService.notifyPayment(UNPAID_ORDER_ID)).thenReturn(true);
        service.statusCheck();
        verify(orderService).notifyPayment(UNPAID_ORDER_ID);
        verify(notifyService).send(UNPAID_ORDER_ID);
    }
    
    @Test
    public void statusCheckerChecksUnpaidButDoesNotNotifyUnpaid() throws ServiceException {
        when(orderService.findUnpaidOrderIds(minAge)).thenReturn(new HashSet<>(asList(UNPAID_ORDER_ID)));
        when(orderService.notifyPayment(UNPAID_ORDER_ID)).thenReturn(false);
        service.statusCheck();
        verify(orderService).notifyPayment(UNPAID_ORDER_ID);
        verifyZeroInteractions(notifyService);
    }
    
    @Test
    public void statusCheckerDoesNotNotifyUnpaidOrders() throws ServiceException {
        doThrow(new ServiceException("expected unpaid")).when(orderService).notifyPayment(UNPAID_ORDER_ID);
        when(orderService.findUnpaidOrderIds(minAge)).thenReturn(new HashSet<String>(asList(UNPAID_ORDER_ID)));
        
        service.statusCheck();
        verify(orderService).notifyPayment(UNPAID_ORDER_ID);
        verifyZeroInteractions(notifyService);
    }
}
