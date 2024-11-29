package au.gov.qld.pub.orders.scenario.selenium;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import au.gov.qld.pub.orders.scenario.ScenarioSetup;

public class ScenarioIT extends ScenarioSetup {

    @Test
    public void addToCart() throws Exception {
        ConfirmPage confirm = new SubmissionPage(getWebDriver()).open().order();
        AddedPage added = confirm.add();
        assertThat(added.isOpen(), is(true));
    }

    @Test
    public void noticeToPay() throws Exception {
        String url = new SubmissionPage(getWebDriver()).open().noticeToPay(true, null, null);
        assertThat(url, containsString("/payment/notice/"));
    }

    @Test
    public void redirectNoticeToPayOnValidationError() throws Exception {
        String url = new SubmissionPage(getWebDriver()).open().noticeToPay(false, "", "");
        assertThat(url, is(BASE_URL + "test"));
    }

    @Test
    public void redirectToError() throws Exception {
        SubmissionPage test = new SubmissionPage(getWebDriver()).open();
        test.clearProductIds();
        test.order();
        assertThat(getWebDriver().getCurrentUrl(), containsString("test"));
    }
}
