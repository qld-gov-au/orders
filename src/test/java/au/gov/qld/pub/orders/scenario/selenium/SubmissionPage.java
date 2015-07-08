package au.gov.qld.pub.orders.scenario.selenium;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;

public class SubmissionPage extends Page {
    WebElement confirm;
    WebElement group;
    WebElement noticeToPay;
    WebElement sourceId;
    WebElement sourceUrl;
    List<WebElement> productIds;
    
    public static SubmissionPage open() {
        getDriver().get(SubmissionPage.URL + "test");
        SubmissionPage submissionPage = PageFactory.initElements(getDriver(), SubmissionPage.class);
        submissionPage.productIds = getDriver().findElements(By.id("productId"));
        return submissionPage;
    }
    
    public ConfirmPage order() {
        confirm.click();
        return PageFactory.initElements(getDriver(), ConfirmPage.class);
    }

    public void clearProductIds() {
        group.clear();
        for (WebElement productId : productIds) {
            productId.clear();
        }
    }

    public String noticeToPay(boolean useDefault, String sourceId, String sourceUrl) {
        if (!useDefault) {
            setText(sourceId, this.sourceId);
            setText(sourceUrl, this.sourceUrl);
        }
        noticeToPay.click();
        return getDriver().getCurrentUrl();
    }
}
