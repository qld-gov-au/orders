package au.gov.qld.pub.orders.scenario.selenium;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;


public class ConfirmPage extends Page {
	private WebElement add;
	
	public AddedPage add() {
		add.click();
		return PageFactory.initElements(getDriver(), AddedPage.class);
	}

}
