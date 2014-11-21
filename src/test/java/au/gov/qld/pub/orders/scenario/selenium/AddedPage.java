package au.gov.qld.pub.orders.scenario.selenium;

public class AddedPage extends Page {

    public boolean isOpen() {
        return getDriver().getCurrentUrl().endsWith("/added");
    }

}
