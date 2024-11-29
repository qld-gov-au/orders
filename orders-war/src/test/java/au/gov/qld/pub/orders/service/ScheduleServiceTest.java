package au.gov.qld.pub.orders.service;

import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.HashSet;

import org.joda.time.DateTimeUtils;
import org.joda.time.LocalDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ScheduleServiceTest {
    private static final String UNPAID_ORDER_ID = "some unpaid order id";
    private static final int MAX_AGE = 10;
    private static final int DELETE_PAID_DAYS = 456;
    private static final int DELETE_UNPAID_DAYS = 678;

    @Mock ConfigurationService config;
    @Mock OrderService orderService;

    Date minAge;
    ScheduleService service;

    @BeforeEach
    public void setUp() throws ServiceException {
        DateTimeUtils.setCurrentMillisFixed(new Date().getTime());
        minAge = new LocalDateTime().minusMillis(MAX_AGE).toDate();
        when(config.getMaxAgeForRetry()).thenReturn(MAX_AGE);
        when(config.getDeletePaidOrderDays()).thenReturn(DELETE_PAID_DAYS);
        when(config.getDeleteUnpaidOrderDays()).thenReturn(DELETE_UNPAID_DAYS);
        service = new ScheduleService(config, orderService);
    }

    @AfterEach
    public void tearDown() {
        DateTimeUtils.setCurrentMillisSystem();
    }

    @Test
    public void cleanupOrdersOlderThanDeleteDaysAgo() {
        service.cleanup();
        verify(orderService).deleteOlderThan(new LocalDateTime().minusDays(DELETE_PAID_DAYS), true);
        verify(orderService).deleteOlderThan(new LocalDateTime().minusDays(DELETE_UNPAID_DAYS), false);
    }

    @Test
    public void doNotCleanupUnpaidOrdersWhenUnpaidOrderDaysNegative() {
        when(config.getDeleteUnpaidOrderDays()).thenReturn(-1);
        service = new ScheduleService(config, orderService);
        service.cleanup();
        verify(orderService).deleteOlderThan(new LocalDateTime().minusDays(DELETE_PAID_DAYS), true);
        verify(orderService, never()).deleteOlderThan(isA(LocalDateTime.class), eq(false));
    }

    @Test
    public void doNotCleanupPaidOrdersWhenPaidOrderDaysNegative() {
        when(config.getDeletePaidOrderDays()).thenReturn(-1);
        service = new ScheduleService(config, orderService);
        service.cleanup();
        verify(orderService, never()).deleteOlderThan(isA(LocalDateTime.class), eq(true));
        verify(orderService).deleteOlderThan(new LocalDateTime().minusDays(DELETE_UNPAID_DAYS), false);
    }

    @Test
    public void statusCheckerChecksUnpaidAndUnnotifiedOrdersAndFulfills() throws ServiceException {
        when(orderService.findUnpaidOrderIds(minAge)).thenReturn(new HashSet<>(asList(UNPAID_ORDER_ID)));
        when(orderService.notifyPayment(UNPAID_ORDER_ID)).thenReturn(true);
        service.statusCheck();
        verify(orderService).notifyPayment(UNPAID_ORDER_ID);
    }

    @Test
    public void statusCheckerChecksUnpaidButDoesNotNotifyUnpaid() throws ServiceException {
        when(orderService.findUnpaidOrderIds(minAge)).thenReturn(new HashSet<>(asList(UNPAID_ORDER_ID)));
        when(orderService.notifyPayment(UNPAID_ORDER_ID)).thenReturn(false);
        service.statusCheck();
        verify(orderService).notifyPayment(UNPAID_ORDER_ID);
    }

    @Test
    public void statusCheckerDoesNotNotifyUnpaidOrders() throws ServiceException {
        doThrow(new ServiceException("expected unpaid")).when(orderService).notifyPayment(UNPAID_ORDER_ID);
        when(orderService.findUnpaidOrderIds(minAge)).thenReturn(new HashSet<String>(asList(UNPAID_ORDER_ID)));

        service.statusCheck();
        verify(orderService).notifyPayment(UNPAID_ORDER_ID);
    }
}
