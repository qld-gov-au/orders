package au.gov.qld.pub.orders.dao;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.joda.time.DateTime;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import au.gov.qld.pub.orders.ApplicationContextAwareTest;
import au.gov.qld.pub.orders.entity.NoticeToPay;
import au.gov.qld.pub.orders.service.PaymentInformationService;

public class NoticeToPayDAOTest extends ApplicationContextAwareTest {
    @Autowired NoticeToPayDAO noticeToPayDao;
    @Autowired PaymentInformationService paymentInformationService;
    
    @Test
    public void returnTrueWhenRecentNoticeToPayPaid() throws Exception {
        NoticeToPay ntpNew = new NoticeToPay(paymentInformationService.fetch("1"));
        ntpNew.setNotifiedAt(new DateTime().toDate());
        
        NoticeToPay ntpOld = new NoticeToPay(paymentInformationService.fetch("2"));
        ntpOld.setNotifiedAt(new DateTime().minusDays(1).toDate());
        
        NoticeToPay ntpOther = new NoticeToPay(paymentInformationService.fetch("3"));
        noticeToPayDao.saveAll(asList(ntpNew, ntpOld, ntpOther));
        
        
        assertThat(noticeToPayDao.existsByPaymentInformationIdAndNotifiedAtAfter("1", new DateTime().minusMinutes(5).toDate()), is(true));
        assertThat(noticeToPayDao.existsByPaymentInformationIdAndNotifiedAtAfter("2", new DateTime().minusMinutes(5).toDate()), is(false));
        assertThat(noticeToPayDao.existsByPaymentInformationIdAndNotifiedAtAfter("3", new DateTime().minusMinutes(5).toDate()), is(false));
    }
}
