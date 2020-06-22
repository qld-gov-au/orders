package au.gov.qld.pub.orders.service;

import static java.util.Arrays.asList;
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
    private static final String PAID_NTP_ID = "3579d7de-928c-4f70-a574-a5a847e5329c";
    private static final String SOURCE_ID = "some id";
    private static final String SOURCE_URL = "http://www.example.com";
    private static final String RECEIPT_NUMBER = "1868456";
    private static final String DESCRIPTION = "some description";
    private static final long AMOUNT = 123;
    private static final long AMOUNT_GST = 45;
    
    @Autowired NoticeToPayService service;
    @Autowired NoticeToPayDAO dao;
    PaymentInformation paymentInformation;
    
    @Before
    public void setUp() throws Exception {
    	OrderInformation order1 = new OrderInformation("product", AMOUNT, AMOUNT_GST, 1);
    	OrderInformation order2 = new OrderInformation("product", 1, 2, 1);
        Applicant applicant = new Applicant("reg name", "addr1", "suburb", "state", "postcode", "country");
		paymentInformation = new PaymentInformation(SOURCE_ID, DESCRIPTION, asList(order1, order2), applicant);
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
        NoticeToPay unpaid = dao.findById(PAID_NTP_ID).get();
        assertThat(unpaid.getReceiptNumber(), nullValue());
        assertThat(unpaid.getNotifiedAt(), nullValue());
        
        service.notifyPayment(PAID_NTP_ID);
        NoticeToPay paid = dao.findById(PAID_NTP_ID).get();
        assertThat(paid.getReceiptNumber(), is(RECEIPT_NUMBER));
        assertThat(paid.getNotifiedAt(), notNullValue());
    }
}
