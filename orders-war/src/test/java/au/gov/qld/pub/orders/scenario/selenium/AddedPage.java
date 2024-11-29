package au.gov.qld.pub.orders.scenario.selenium;

import org.openqa.selenium.WebDriver;

public class AddedPage extends Page {

    public AddedPage(WebDriver webDriver) {
        super(webDriver);
    }
    public boolean isOpen() {
        return webDriver.getCurrentUrl().endsWith("/added");
    }

}
