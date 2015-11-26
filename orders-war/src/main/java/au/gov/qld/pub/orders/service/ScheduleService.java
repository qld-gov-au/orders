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
    private final NotifyService notifyService;
    private final int maxAge;
    
    @Autowired
    public ScheduleService(ConfigurationService config, OrderService orderService, NotifyService notifyService) {
        this.maxAge = config.getMaxAgeForRetry();
        this.orderService = orderService;
        this.notifyService = notifyService;
    }
    
    public void statusCheck() {
        LOG.info("Scheduled task: {} starting", "statusCheck");
        Date minCreated = new LocalDateTime().minusMillis(maxAge).toDate();
        
        for (String orderId : orderService.findUnpaidOrderIds(minCreated)) {
            try {
                orderService.notifyPayment(orderId);
                notifyService.send(orderId);
            } catch (ServiceException e) {
                LOG.info(e.getMessage(), e);
            }
        }
        
        LOG.info("Scheduled task: {} finished", "statusCheck");
    }
    
}
