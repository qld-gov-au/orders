package au.gov.qld.pub.orders.scenario.selenium;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import au.gov.qld.pub.orders.scenario.ScenarioSetup;

public class Page {
    public static final String URL = ScenarioSetup.BASE_URL;

    protected WebDriver webDriver;

    public Page(WebDriver webDriver){
        this.webDriver = webDriver;
    }

    public void selectOption(WebElement element, String value) {
        List<WebElement> options = element.findElements(By.tagName("option"));
        for (WebElement option : options) {
            if (value.equals(option.getText())) {
                option.click();
                return;
            }
        }
        fail("Could not select " + value);
    }

    public String getText() {
        return webDriver.getPageSource();
    }


    public static void select(WebElement element, String text) {
        Map<String, WebElement> options = elementsByText(element.findElements(By.xpath("option")));
        if (options.containsKey(text)) {
            options.get(text).click();
            return;
        }

        throw new NoSuchElementException("No option with text: " + text + " in " + options.keySet());
    }

    public static Map<String, WebElement> elementsByText(List<WebElement> elements) {
        Map<String, WebElement> results = new LinkedHashMap<String, WebElement>();
        for (WebElement element : elements) {
            results.put(element.getText().trim(), element);
        }
        return results;
    }

    protected void setText(String text, WebElement element) {
        element.clear();
        element.sendKeys(text);
    }
}
