package au.gov.qld.pub.orders.service;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.defaultString;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import au.gov.qld.pub.orders.dao.NoticeToPayDAO;
import au.gov.qld.pub.orders.entity.NoticeToPay;
import au.gov.qld.pub.orders.service.ws.RequestBuilder;
import au.gov.qld.pub.orders.service.ws.SOAPClient;

@Service
public class NoticeToPayService {
    private static final Logger LOG = LoggerFactory.getLogger(NotifyService.class);
    public static final String NS = "http://smartservice.qld.gov.au/payment/schemas/notice_to_pay_1_4";
    private static final String PAID_STATUS = "PAID";
    private static final String FAILED_STATUS = "FAILED";
    private static final Set<String> VALID_STATUSES = new HashSet<>(asList(PAID_STATUS, FAILED_STATUS));
    private static final Pattern RESPONSE_PATTERN = Pattern.compile("<.*[:]?redirectUrl>(.+)</.*[:]?redirectUrl>");
    private static final Pattern STATUS_PATTERN = Pattern.compile("<.*[:]?status>(.+)</.*[:]?status>");
    private static final Pattern RECEIPT_NUMBER_PATTERN = Pattern.compile("<.*[:]?receiptNumber>(.+)</.*[:]?receiptNumber>");
    private final NoticeToPayDAO noticeToPayDAO;
    private final RequestBuilder requestBuilder;
    private final SOAPClient soapClient;
    private final String username;
    private final byte[] password;
    private final PaymentInformationService paymentInformationService;
	private final AdditionalNotificationService additionalNotificationService;

    @Autowired
    public NoticeToPayService(ConfigurationService config, PaymentInformationService paymentInformationService, 
            NoticeToPayDAO noticeToPayDAO, RequestBuilder requestBuilder, AdditionalNotificationService additionalNotificationService) throws UnsupportedEncodingException {
        this.paymentInformationService = paymentInformationService;
		this.additionalNotificationService = additionalNotificationService;
        this.username = config.getNoticeToPayServiceWsUsername();
        this.password = config.getNoticeToPayServiceWsPassword().getBytes("UTF-8");
        this.soapClient = getSOAPClient(config.getServiceWsEndpoint());
        this.noticeToPayDAO = noticeToPayDAO;
        this.requestBuilder = requestBuilder;
    }

    @Transactional
    public String create(String sourceId, String sourceUrl) throws ServiceException {
        PaymentInformation paymentInformation = paymentInformationService.fetch(sourceId);
        if (paymentInformation.getAmountOwingInCents() <= 0l) {
        	LOG.warn("No amount owing for {}", sourceId);
            return null;
        }
        
        boolean recentlyPaid = noticeToPayDAO.existsByPaymentInformationIdAndNotifiedAtAfter(sourceId, new DateTime().minusHours(1).toDate());
        if (recentlyPaid) {
        	LOG.warn("This source ID was recently paid for and could be a duplicate: {}", sourceId);
        	return null;
        }

        NoticeToPay noticeToPay = new NoticeToPay(paymentInformation);
        noticeToPayDAO.save(noticeToPay);
        String request = requestBuilder.noticeToPay(paymentInformation, noticeToPay.getId() , sourceUrl);
        
        LOG.info("Sending notice to pay request for source ID: {} and NTP ID: {}", sourceId, noticeToPay.getId());
        String response = soapClient.sendRequest(username, password, NS, request);
        Matcher matcher = RESPONSE_PATTERN.matcher(response);
        if (!matcher.find()) {
            throw new ServiceException("Unhandled response from payment gateway");
        }
        
        String redirect = matcher.group(1);
        LOG.info("Received redirect to: {}", redirect);
        return redirect;
    }

    protected SOAPClient getSOAPClient(String endpoint) {
        return new SOAPClient(endpoint);
    }

    @Transactional
    public void notifyPayment(String noticeToPayId) throws ServiceException {
        final Date now = new Date();
        
        NoticeToPay noticeToPay = noticeToPayDAO.findOne(noticeToPayId);
        if (noticeToPay == null) {
            throw new ServiceException("Unknown notice to pay");
        }
        
        if (noticeToPay.getNotifiedAt() != null) {
            LOG.info("Already received notification for: {}", noticeToPayId);
            return;
        }
        
        String request = requestBuilder.noticeToPayQuery(noticeToPayId);
        String response = defaultString(soapClient.sendRequest(username, password, NS, request));
        if (isNotPaid(response)) {
            LOG.info("Received notification for unpaid");
            return;
        }
        
        String receiptNumber = getReceiptNumber(response);        
        LOG.info("Received payment notification for: {} with receipt: {}", noticeToPayId, receiptNumber);
        noticeToPay.setNotifiedAt(now);
        noticeToPay.setReceiptNumber(receiptNumber);
        additionalNotificationService.notifedPaidNoticeToPay(noticeToPayId, now, receiptNumber, noticeToPay.getAmount(), noticeToPay.getAmountGst(),
        		noticeToPay.getDescription(), noticeToPay.getPaymentInformationId());
        noticeToPayDAO.save(noticeToPay);
    }

    private String getReceiptNumber(String response) {
        Matcher receiptMatcher = RECEIPT_NUMBER_PATTERN.matcher(response);
        receiptMatcher.find();
        return receiptMatcher.group(1);
    }

    private boolean isNotPaid(String response) throws ServiceException {
        Matcher matcher = STATUS_PATTERN.matcher(response);
        matcher.find();
        
        final String status = matcher.group(1);
        if (VALID_STATUSES.contains(status)) {
            return !PAID_STATUS.equals(status);
        }
        
        throw new ServiceException("Notification on NTP with invalid response from payment gateway: " + response); 
    }
}
