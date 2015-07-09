package au.gov.qld.pub.orders.service;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import au.gov.qld.pub.orders.ApplicationContextAwareTest;
import au.gov.qld.pub.orders.dao.NoticeToPayDAO;
import au.gov.qld.pub.orders.entity.NoticeToPay;

public class NoticeToPayServiceIntegrationTest extends ApplicationContextAwareTest {
	private static final String PAID_NTP_ID = "292284ff-a4fe-4304-a7fe-9e5575be91cb";
    private static final String SOURCE_ID = "some id";
	private static final String SOURCE_URL = "http://www.example.com";
    private static final String RECEIPT_NUMBER = "1866179";
    private static final String DESCRIPTION = "some description";
    private static final long AMOUNT = 123;
    private static final long AMOUNT_GST = 45;
	
	@Autowired NoticeToPayService service;
	@Autowired NoticeToPayDAO dao;
    PaymentInformation paymentInformation;
	
	@Before
    public void setUp() throws Exception {
	    paymentInformation = new PaymentInformation(SOURCE_ID, DESCRIPTION, AMOUNT, AMOUNT_GST);
	    dao.deleteAll();
	}
	
	@Test
	public void returnRedirectForFetchedAndSavedPaymentInformation() throws Exception {
		String redirect = service.create(SOURCE_ID, SOURCE_URL);
		assertThat(redirect, containsString("/payment/notice/"));
	}
	
	@Test
	public void setPaymentOnNoticeToPay() throws ServiceException {
	    dao.save(new NoticeToPay(PAID_NTP_ID, paymentInformation));
	    NoticeToPay unpaid = dao.findOne(PAID_NTP_ID);
	    assertThat(unpaid.getReceiptNumber(), nullValue());
        assertThat(unpaid.getNotifiedAt(), nullValue());
	    
	    service.notifyPayment(PAID_NTP_ID);
	    NoticeToPay paid = dao.findOne(PAID_NTP_ID);
	    assertThat(paid.getReceiptNumber(), is(RECEIPT_NUMBER));
	    assertThat(paid.getNotifiedAt(), notNullValue());
	}
}
