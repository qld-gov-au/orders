package au.gov.qld.pub.orders.service;

import java.io.UnsupportedEncodingException;
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
	private static final Pattern RESPONSE_PATTERN = Pattern.compile("<redirectUrl>(.+)</redirectUrl>");
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

	// TODO: make sure to support multiple payments on the same id.
	// TODO: consider caching the payment information response.
	// TODO: consider creating surrogate id and tying it to a user cookie since the provided id is just a sequence, especially if going to re-use a NTP.
	// TODO: if going to be re-using notice to pay, make sure to use UUID rather than source id, or re-create the whole NTP to make sure NTPs cant be used by someone else other than the originator.	
	@Transactional
	public String create(String sourceId, String sourceUrl) throws ServiceException {
		PaymentInformation paymentInformation = paymentInformationService.fetch(sourceId);
		if (paymentInformation.getAmountOwingInCents() <= 0l) {
			throw new ServiceException("No amount owing for " + sourceId);
		}

		NoticeToPay noticeToPay = new NoticeToPay(sourceId);
		noticeToPayDAO.save(noticeToPay);
		String request = requestBuilder.noticeToPay(paymentInformation, noticeToPay.getId() , sourceUrl);
		
		LOG.info("Sending notice to pay request for source ID: {}", sourceId);
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
}
