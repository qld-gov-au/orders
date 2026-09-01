package au.gov.qld.pub.orders.dao;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import au.gov.qld.pub.orders.utils.DateTimeUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import au.gov.qld.pub.orders.ApplicationContextAwareTest;
import au.gov.qld.pub.orders.entity.NoticeToPay;
import au.gov.qld.pub.orders.service.PaymentInformationService;

import java.time.temporal.ChronoUnit;

public class NoticeToPayDAOIT extends ApplicationContextAwareTest {
    @Autowired NoticeToPayDAO noticeToPayDao;
    @Autowired PaymentInformationService paymentInformationService;

    @Test
    public void returnTrueWhenRecentNoticeToPayPaid() throws Exception {
        NoticeToPay ntpNew = new NoticeToPay(paymentInformationService.fetch("1"));
        ntpNew.setNotifiedAt(DateTimeUtils.currentLocalDateTime());

        NoticeToPay ntpOld = new NoticeToPay(paymentInformationService.fetch("2"));
        ntpOld.setNotifiedAt(DateTimeUtils.currentLocalDateTime().minusDays(1));

        NoticeToPay ntpOther = new NoticeToPay(paymentInformationService.fetch("3"));
        noticeToPayDao.saveAll(asList(ntpNew, ntpOld, ntpOther));


        assertThat(noticeToPayDao.existsByPaymentInformationIdAndNotifiedAtAfter("1", DateTimeUtils.currentLocalDateTime().minusMinutes(5)), is(true));
        assertThat(noticeToPayDao.existsByPaymentInformationIdAndNotifiedAtAfter("2", DateTimeUtils.currentLocalDateTime().minusMinutes(5)), is(false));
        assertThat(noticeToPayDao.existsByPaymentInformationIdAndNotifiedAtAfter("3", DateTimeUtils.currentLocalDateTime().minusMinutes(5)), is(false));
    }
}
