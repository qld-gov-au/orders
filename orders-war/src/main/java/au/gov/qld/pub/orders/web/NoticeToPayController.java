package au.gov.qld.pub.orders.web;

import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public RedirectView payInFull(@RequestParam("sourceId") String sourceId, @RequestParam("sourceUrl") String sourceUrl) {
        String trimmedSourceUrl = defaultString(sourceUrl).trim();
        if (!validateInput(trimmedSourceUrl, sourcePattern)) {
            LOG.info("Invalid source url");
            return WebUtils.redirect(defaultRedirect);
        }

        String trimmedSourceId = defaultString(sourceId).trim();
        if (!validateInput(trimmedSourceId, idPattern)) {
            LOG.info("Invalid source ID");
            return WebUtils.redirect(trimmedSourceUrl);
        }

        LOG.info("Creating notice to pay for {}", trimmedSourceId);
        try {
            String redirect = service.create(trimmedSourceId, trimmedSourceUrl);
            if (isBlank(redirect)) {
            	return WebUtils.redirect(defaultRedirect);
            }

			return WebUtils.redirect(redirect);
        } catch (ServiceException e) {
            LOG.error(e.getMessage(), e);
            return WebUtils.redirect(defaultRedirect);
        }
    }

    @RequestMapping(value = "/ntp-notify/{noticeToPayId}", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<String> notifyPayment(@PathVariable("noticeToPayId") String noticeToPayId) throws ServiceException {
        if (isBlank(noticeToPayId) || noticeToPayId.trim().length() >= MAX_NOTICE_TO_PAY_ID_LENGTH) {
            throw new ServiceException("Invalid notice to pay id");
        }

        service.notifyPayment(noticeToPayId);
        return new ResponseEntity<String>(HttpStatus.OK);
    }

    private boolean validateInput(String value, Pattern pattern) {
        return isNotBlank(value) && pattern.matcher(value).matches();
    }


}
