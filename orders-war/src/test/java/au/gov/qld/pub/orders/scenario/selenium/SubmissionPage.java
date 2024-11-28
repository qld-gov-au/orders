package au.gov.qld.pub.orders.scenario.selenium;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;

public class SubmissionPage extends Page {
    WebElement confirm;
    WebElement group;
    WebElement noticeToPay;
    WebElement sourceId;
    WebElement sourceUrl;
    List<WebElement> productIds;

    public SubmissionPage(WebDriver webDriver) {
        super(webDriver);
    }

    public SubmissionPage open() {
        webDriver.get(SubmissionPage.URL + "test");
        SubmissionPage submissionPage = PageFactory.initElements(webDriver, SubmissionPage.class);
        submissionPage.productIds = webDriver.findElements(By.id("productId"));
        return submissionPage;
    }

    public ConfirmPage order() {
        confirm.click();
        return PageFactory.initElements(webDriver, ConfirmPage.class);
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
        return webDriver.getCurrentUrl();
    }
}
