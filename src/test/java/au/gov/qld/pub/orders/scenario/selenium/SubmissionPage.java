package au.gov.qld.bdm.orders.scenario.selenium;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;

public class SubmissionPage extends Page {
	WebElement confirm;
	WebElement group;
	List<WebElement> productIds;
	
	public static SubmissionPage open() {
		getDriver().get(SubmissionPage.URL + "test");
        SubmissionPage submissionPage = PageFactory.initElements(getDriver(), SubmissionPage.class);
        submissionPage.productIds = getDriver().findElements(By.id("productId"));
		return submissionPage;
	}
	
	public ConfirmPage confirm() {
		confirm.click();
		return PageFactory.initElements(getDriver(), ConfirmPage.class);
	}

	public void clearProductIds() {
		group.clear();
		for (WebElement productId : productIds) {
			productId.clear();
		}
	}
}
