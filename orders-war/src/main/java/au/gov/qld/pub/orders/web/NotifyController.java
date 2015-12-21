package au.gov.qld.pub.orders.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import au.gov.qld.pub.orders.service.OrderService;

@Controller
public class NotifyController {
    private static final Logger LOG = LoggerFactory.getLogger(NotifyController.class);
    private final OrderService orderService;

    @Autowired
    public NotifyController(OrderService orderService) {
        this.orderService = orderService;
    }
    
    @RequestMapping(value = "/notify/{orderId}")
    public ResponseEntity<String> confirm(@PathVariable String orderId) {
        LOG.info("Received notify for order id: {}", orderId);
        try {
            orderService.notifyPayment(orderId);
            return new ResponseEntity<String>(HttpStatus.OK);
        } catch (Exception e) {
            LOG.warn(e.getMessage(), e);
            return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
        }
    }
        
    
}
