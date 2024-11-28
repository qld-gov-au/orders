package au.gov.qld.pub.orders.scenario.selenium;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;


public class ConfirmPage extends Page {
    private WebElement add;

    public ConfirmPage(WebDriver webDriver) {
        super(webDriver);
    }

    public AddedPage add() {
        add.click();
        return PageFactory.initElements(webDriver, AddedPage.class);
    }

}
