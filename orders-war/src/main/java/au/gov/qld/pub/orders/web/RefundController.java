package au.gov.qld.pub.orders.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import au.gov.qld.pub.orders.service.refund.RefundService;

@Controller
public class RefundController {
    private static final Logger LOG = LoggerFactory.getLogger(RefundController.class);
    private final RefundService refundService;

    @Autowired
    public RefundController(RefundService refundService) {
        this.refundService = refundService;
    }
    
    @RequestMapping(value = "/refund/new")
    public ResponseEntity<String> refundNew() {
        LOG.info("Received refund new request");
        try {
        	refundService.refundNewItems();
            return new ResponseEntity<String>(HttpStatus.OK);
        } catch (Exception e) {
            LOG.warn(e.getMessage(), e);
            return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
        
    
}
