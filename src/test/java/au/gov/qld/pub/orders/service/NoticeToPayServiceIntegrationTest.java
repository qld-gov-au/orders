package au.gov.qld.pub.orders.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import au.gov.qld.pub.orders.ApplicationContextAwareTest;

public class NoticeToPayServiceIntegrationTest extends ApplicationContextAwareTest {
	private static final String SOURCE_ID = "some id";
	private static final String SOURCE_URL = "http://www.example.com";
	
	@Autowired NoticeToPayService service;
	
	@Before
    public void setUp() throws Exception {
	}
	
	@Test
	public void returnRedirectForFetchedAndSavedPaymentInformation() throws Exception {
		String redirect = service.create(SOURCE_ID, SOURCE_URL);
		assertThat(redirect, containsString("/payment/notice/"));
	}
}
