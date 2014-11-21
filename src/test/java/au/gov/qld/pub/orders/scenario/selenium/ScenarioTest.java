package au.gov.qld.pub.orders.scenario.selenium;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

import au.gov.qld.pub.orders.scenario.ScenarioSetup;

public class ScenarioTest extends ScenarioSetup {
    
    @Test
    public void addToCart() throws Exception {
        ConfirmPage confirm = SubmissionPage.open().confirm();
        AddedPage added = confirm.add();
        assertThat(added.isOpen(), is(true));
    }
    
    @Test
    public void redirectToError() throws Exception {
        SubmissionPage test = SubmissionPage.open();
        test.clearProductIds();
        test.confirm();
        assertThat(driver.getCurrentUrl(), containsString("test"));
    }
}
