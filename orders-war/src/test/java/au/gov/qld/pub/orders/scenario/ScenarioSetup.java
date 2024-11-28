package au.gov.qld.pub.orders.scenario;

import java.lang.invoke.MethodHandles;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.time.Duration;

import au.gov.qld.online.selenium.DriverTypes;
import au.gov.qld.online.selenium.SeleniumHelper;
import au.gov.qld.online.selenium.WebDriverHolder;
import com.dumbster.smtp.SimpleSmtpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;

import au.gov.qld.pub.orders.scenario.selenium.ConfirmPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScenarioSetup {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static final String BASE_URL = "http://localhost:8091/";

    protected WebDriverHolder webDriverHolder;

    private static SimpleSmtpServer mailServer;


    @BeforeAll
    public static void startMailServer() {
        try {
            System.out.println("Starting embedded instance");
            mailServer = SimpleSmtpServer.start(1325); //ensure same in application.properties spring.mail.port
        } catch (Exception startEx) {
            //Ignore
        }
    }

    @AfterAll
    public static void stopMailServer() {
        try {
            if (mailServer != null) {
                mailServer.stop();
            }
        } catch (Exception e) {
            //Ignore
        }
    }

    @BeforeEach
    public void setUp() {
        webDriverHolder = SeleniumHelper.getWebDriver(DriverTypes.CHROME);
        webDriverHolder.getWebDriver().manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
        webDriverHolder.getWebDriver().get(ConfirmPage.URL);
    }

    @AfterEach
    public void tearDown() {
        LOG.info("thread: {}, in JVM: {} is ending", Thread.currentThread().getId(), ManagementFactory.getRuntimeMXBean().getName());
        if (webDriverHolder != null) {
            SeleniumHelper.close(webDriverHolder);
        }
    }

    public WebDriver getWebDriver() {
        return webDriverHolder.getWebDriver();
    }
}
