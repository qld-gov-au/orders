package au.gov.qld.bdm.orders.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import au.gov.qld.bdm.orders.service.NotifyService;
import au.gov.qld.bdm.orders.service.OrderService;

@Controller
public class NotifyController {
	private static final Logger LOG = LoggerFactory.getLogger(NotifyController.class);
	private final OrderService orderService;
	private final NotifyService notifyService;

	@Autowired
	public NotifyController(OrderService orderService, NotifyService notifyService) {
		this.orderService = orderService;
		this.notifyService = notifyService;
	}
	
	@RequestMapping(value = "/notify/{orderId}")
	public ResponseEntity<String> confirm(@PathVariable String orderId) {
		LOG.info("Received notify for order id: {}", orderId);
		try {
			orderService.notifyPayment(orderId);
			notifyService.send(orderId);
			
			return new ResponseEntity<String>(HttpStatus.OK);
		} catch (Exception e) {
			LOG.warn(e.getMessage(), e);
			return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
		}
	}
		
	
}
