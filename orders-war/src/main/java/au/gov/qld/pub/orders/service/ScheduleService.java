package au.gov.qld.pub.orders.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import au.gov.qld.pub.orders.utils.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ScheduleService {
    private static final Logger LOG = LoggerFactory.getLogger(ScheduleService.class);
    private final OrderService orderService;
    private final int maxAge;
    private final int cleanupPaidOrderDays;
    private final int cleanupUnpaidOrderDays;


    @Autowired
    public ScheduleService(ConfigurationService config, OrderService orderService) {
        this.maxAge = config.getMaxAgeForRetry();
        this.cleanupPaidOrderDays = config.getDeletePaidOrderDays();
        this.cleanupUnpaidOrderDays = config.getDeleteUnpaidOrderDays();
        this.orderService = orderService;
    }

    @Scheduled(fixedDelayString = "${scheduler.statusCheck.delay}")
    public void statusCheck() {
        LOG.info("Scheduled task: {} starting", "statusCheck");
        LocalDateTime minCreated = DateTimeUtils.currentLocalDateTime()
                .minus(maxAge, ChronoUnit.MILLIS);

        for (String orderId : orderService.findUnpaidOrderIds(minCreated)) {
            try {
                orderService.notifyPayment(orderId);
            } catch (ServiceException e) {
                LOG.info(e.getMessage(), e);
            }
        }

        LOG.info("Scheduled task: {} finished", "statusCheck");
    }

    @Scheduled(fixedDelay = 3600000l)
    public void cleanup() {
        LOG.info("Scheduled task: {} starting", "cleanup");
        if (cleanupPaidOrderDays >= 0) {
            orderService.deleteOlderThan(DateTimeUtils.currentLocalDateTime().minusDays(cleanupPaidOrderDays), true);
        }
        if (cleanupUnpaidOrderDays >= 0) {
            orderService.deleteOlderThan(DateTimeUtils.currentLocalDateTime().minusDays(cleanupUnpaidOrderDays), false);
        }
        LOG.info("Scheduled task: {} finished", "cleanup");
    }

}
