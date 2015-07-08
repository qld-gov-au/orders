package au.gov.qld.pub.orders.web;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import au.gov.qld.pub.orders.service.ConfigurationService;
import au.gov.qld.pub.orders.service.NoticeToPayService;
import au.gov.qld.pub.orders.service.ServiceException;

@Controller
public class NoticeToPayController {
    private static final Logger LOG = LoggerFactory.getLogger(NoticeToPayController.class);
    private static final int MAX_NOTICE_TO_PAY_ID_LENGTH = 100;
    
    private final String defaultRedirect;
	private final Pattern sourcePattern;
	private final Pattern idPattern;
	private final NoticeToPayService service;
	
    @Autowired
    public NoticeToPayController(ConfigurationService config, NoticeToPayService service) {
		this.service = service;
		this.defaultRedirect = config.getNoticeToPayDefaultRedirect();
		this.sourcePattern = Pattern.compile(config.getNoticeToPaySourcePattern());
		this.idPattern = Pattern.compile(config.getNoticeToPayIdPattern());
    }
    
    @RequestMapping(value = "/pay-in-full", method = {RequestMethod.GET, RequestMethod.POST})
    public RedirectView payInFull(@RequestParam String sourceId, @RequestParam String sourceUrl) throws ServiceException {
    	if (!validateInput(sourceUrl, sourcePattern)) {
    	    LOG.info("Invalid source url");
    	    return WebUtils.redirect(defaultRedirect);
    	}
    	
    	if (!validateInput(sourceId, idPattern)) {
    	    LOG.info("Invalid source ID");
    	    return WebUtils.redirect(sourceUrl);
    	}
    	
    	LOG.info("Creating notice to pay for {}", sourceId);
    	return WebUtils.redirect(service.create(sourceId, sourceUrl));
    }
    
    @RequestMapping(value = "/ntp-notify/{noticeToPayId}", method = {RequestMethod.GET, RequestMethod.POST})
    public void notifyPayment(@PathVariable String noticeToPayId) throws ServiceException {
        if (isBlank(noticeToPayId) || noticeToPayId.trim().length() >= MAX_NOTICE_TO_PAY_ID_LENGTH) {
            throw new ServiceException("Invalid notice to pay id");
        }
        
        service.notifyPayment(noticeToPayId);
    }

	private boolean validateInput(String value, Pattern pattern) {
		return isNotBlank(value) && pattern.matcher(value).matches();
	}

    
}
