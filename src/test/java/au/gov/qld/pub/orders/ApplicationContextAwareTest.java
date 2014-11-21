package au.gov.qld.bdm.orders;


import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.dumbster.smtp.SimpleSmtpServer;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext.xml"})
@Transactional
public abstract class ApplicationContextAwareTest {
	public static String LINE_SEPARATOR = System.getProperty("line.separator");
	
	@Value("${mail.port}") private Integer mailPort;
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
