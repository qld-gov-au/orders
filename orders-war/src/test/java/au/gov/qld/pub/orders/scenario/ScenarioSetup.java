package au.gov.qld.pub.orders.scenario;

import java.net.URL;

import com.dumbster.smtp.SimpleSmtpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import au.gov.qld.pub.orders.scenario.selenium.ConfirmPage;

public class ScenarioSetup {

    public static final String BASE_URL = "http://localhost:8091/";
    private static boolean embedded = false;

    public static WebDriver driver;
    private static SimpleSmtpServer mailServer;

    @BeforeAll
    public static void setupDriver() {
        driver = new HtmlUnitDriver(false);
    }

    @AfterAll
    public static void teardownDriver() {
        driver.quit();
    }

    @BeforeAll
    public static void startJetty() throws Exception {
        try {
            //new URL(BASE_URL).openConnection().getInputStream();
            System.out.println("Using existing instance");
        } catch (Exception e) {
            try {
                System.out.println("Starting embedded instance");
                mailServer = SimpleSmtpServer.start(1325); //ensure same in application.properties spring.mail.port
//                JettyServer.start();
            } catch (Exception startEx) {}
            embedded = true;
        }
    }

    @BeforeAll
    public static void stopJetty() throws Exception {
        try{
            if (embedded) {
                if (mailServer != null) {
                    mailServer.stop();
                }
//                JettyServer.stop();
            }
        } catch (Exception e) {}
    }

    @BeforeEach
    public void setUp() {
        driver.get(ConfirmPage.URL);
    }

}
