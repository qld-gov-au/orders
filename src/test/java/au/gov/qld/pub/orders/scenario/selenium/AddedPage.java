package au.gov.qld.bdm.orders.scenario.selenium;

public class AddedPage extends Page {

	public boolean isOpen() {
		return getDriver().getCurrentUrl().endsWith("/added");
	}

}
