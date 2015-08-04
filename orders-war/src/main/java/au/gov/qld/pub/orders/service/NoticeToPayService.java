package au.gov.qld.pub.orders.service;

import static org.apache.commons.lang3.StringUtils.defaultString;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private static final Pattern RESPONSE_PATTERN = Pattern.compile("<.*[:]?redirectUrl>(.+)</.*[:]?redirectUrl>");
    private static final Pattern STATUS_PATTERN = Pattern.compile("<.*[:]?status>(.+)</.*[:]?status>");
    private static final Pattern RECEIPT_NUMBER_PATTERN = Pattern.compile("<.*[:]?receiptNumber>(.+)</.*[:]?receiptNumber>");
    private final NoticeToPayDAO noticeToPayDAO;
    private final RequestBuilder requestBuilder;
    private final SOAPClient soapClient;
    private final String username;
    private final byte[] password;
    private final PaymentInformationService paymentInformationService;

    @Autowired
    public NoticeToPayService(ConfigurationService config, PaymentInformationService paymentInformationService, 
            NoticeToPayDAO noticeToPayDAO, RequestBuilder requestBuilder) throws UnsupportedEncodingException {
        this.paymentInformationService = paymentInformationService;
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
            throw new ServiceException("No amount owing for " + sourceId);
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
        verifyPaid(response);
        
        String receiptNumber = getReceiptNumber(response);        
        LOG.info("Received payment notification for: {} with receipt: {}", noticeToPayId, receiptNumber);
        noticeToPay.setNotifiedAt(now);
        noticeToPay.setReceiptNumber(receiptNumber);
        noticeToPayDAO.save(noticeToPay);
    }

    private String getReceiptNumber(String response) {
        Matcher receiptMatcher = RECEIPT_NUMBER_PATTERN.matcher(response);
        receiptMatcher.find();
        return receiptMatcher.group(1);
    }

    private void verifyPaid(String response) throws ServiceException {
        Matcher matcher = STATUS_PATTERN.matcher(response);
        if (!matcher.find() || !PAID_STATUS.equals(matcher.group(1))) {
            throw new ServiceException("Notification on NTP with invalid response from payment gateway: " + response);
        }
    }
}