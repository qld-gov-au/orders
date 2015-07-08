package au.gov.qld.pub.orders.scenario.selenium;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

import au.gov.qld.pub.orders.scenario.ScenarioSetup;

public class ScenarioTest extends ScenarioSetup {
    
    @Test
    public void addToCart() throws Exception {
        ConfirmPage confirm = SubmissionPage.open().order();
        AddedPage added = confirm.add();
        assertThat(added.isOpen(), is(true));
    }
    
    @Test
    public void noticeToPay() throws Exception {
        String url = SubmissionPage.open().noticeToPay(true, null, null);
        assertThat(url, containsString("/payment/notice/"));
    }
    
    @Test
    public void redirectNoticeToPayOnValidationError() throws Exception {
        String url = SubmissionPage.open().noticeToPay(false, "", "");
        assertThat(url, is(BASE_URL + "test"));
    }
    
    @Test
    public void redirectToError() throws Exception {
        SubmissionPage test = SubmissionPage.open();
        test.clearProductIds();
        test.order();
        assertThat(driver.getCurrentUrl(), containsString("test"));
    }
}
