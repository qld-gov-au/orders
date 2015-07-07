package au.gov.qld.pub.orders.web;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import au.gov.qld.pub.orders.service.ConfigurationService;
import au.gov.qld.pub.orders.service.NoticeToPayService;
import au.gov.qld.pub.orders.service.ServiceException;

@Controller
public class NoticeToPayController {
    private static final Logger LOG = LoggerFactory.getLogger(NoticeToPayController.class);
    
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
    
    @RequestMapping(value = "/pay-in-full")
    public RedirectView payInFull(@RequestParam String id, @RequestParam String source) throws ServiceException {
    	if (!validateInput(source, sourcePattern)) {
    		return new RedirectView(defaultRedirect);
    	}
    	
    	if (!validateInput(id, idPattern)) {
    		return new RedirectView(source);
    	}
    	
    	LOG.info("Creating notice to pay for {}", id);
    	return new RedirectView(service.create(id, source));
    }

	private boolean validateInput(String value, Pattern pattern) {
		return isNotBlank(value) && pattern.matcher(value).matches();
	}

    
}
