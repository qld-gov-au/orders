package au.gov.qld.pub.orders;


import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import au.gov.qld.pub.orders.config.App;
import au.gov.qld.pub.orders.config.Config;
import au.gov.qld.pub.orders.config.OrderExtensionConfig;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.dumbster.smtp.SimpleSmtpServer;

@SpringBootTest(classes = App.class)
@RunWith(SpringRunner.class)
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(classes={Config.class, OrderExtensionConfig.class}, loader= AnnotationConfigContextLoader.class)
//@WebAppConfiguration
@Transactional
public abstract class ApplicationContextAwareTest {
    public static String LINE_SEPARATOR = System.getProperty("line.separator");

    static {
        String location = System.getProperty("user.home") + File.separator + ".orders.key";
        File keyFile = new File(location);
        if (!keyFile.exists()) {
            System.err.println("Creating test key");
            try {
                FileUtils.write(keyFile, "testkey", Charset.defaultCharset());
            } catch (IOException e) {
                throw new IllegalStateException("Could not create test key");
            }
        }
    }

    @Value("${spring.mail.port}") private Integer mailPort;
    protected SimpleSmtpServer mailServer;

    @Before
    public void beforeMethod() {
        try {
            mailServer = SimpleSmtpServer.start(mailPort);
        } catch (Exception e) {}
    }

    @After
    public void afterMethod() {
        try {
            if (mailServer != null) {
                mailServer.stop();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
