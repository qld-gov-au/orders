package au.gov.qld.pub.orders.scenario;

import java.net.URL;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import au.gov.qld.pub.orders.JettyServer;
import au.gov.qld.pub.orders.scenario.selenium.ConfirmPage;

public class ScenarioSetup {

    public static final String BASE_URL = "http://localhost:8091/orders/";
    private static boolean embedded = false;

    public static WebDriver driver;
    
    @BeforeClass
    public static void setupDriver() {
        driver = new HtmlUnitDriver(false);
    }
    
    @AfterClass
    public static void teardownDriver() {
        driver.quit();
    }
    
    @BeforeClass
    public static void startJetty() throws Exception {
        try {
            new URL(BASE_URL).openConnection().getInputStream();
            System.out.println("Using existing instance");
        } catch (Exception e) {
            try {
                System.out.println("Starting embedded instance");
                JettyServer.start();
            } catch (Exception startEx) {}
            embedded = true;
        }
    }

    @BeforeClass
    public static void stopJetty() throws Exception {
        try{
            if (embedded) {
                JettyServer.stop();
            }
        } catch (Exception e) {}
    }

    @Before
    public void setUp() {
        driver.get(ConfirmPage.URL);
    }

}
