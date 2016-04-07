package au.gov.qld.pub.orders.service;

import java.util.Date;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    
    public void statusCheck() {
        LOG.info("Scheduled task: {} starting", "statusCheck");
        Date minCreated = new LocalDateTime().minusMillis(maxAge).toDate();
        
        for (String orderId : orderService.findUnpaidOrderIds(minCreated)) {
            try {
                orderService.notifyPayment(orderId);
            } catch (ServiceException e) {
                LOG.info(e.getMessage(), e);
            }
        }
        
        LOG.info("Scheduled task: {} finished", "statusCheck");
    }

    public void cleanup() {
        LOG.info("Scheduled task: {} starting", "cleanup");
        if (cleanupPaidOrderDays >= 0) {
            orderService.deleteOlderThan(new LocalDateTime().minusDays(cleanupPaidOrderDays), true);
        }
        if (cleanupUnpaidOrderDays >= 0) {
            orderService.deleteOlderThan(new LocalDateTime().minusDays(cleanupUnpaidOrderDays), false);
        }
        LOG.info("Scheduled task: {} finished", "cleanup");
    }
    
}
