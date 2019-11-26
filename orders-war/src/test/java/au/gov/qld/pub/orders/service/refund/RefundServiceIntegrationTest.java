package au.gov.qld.pub.orders.service.refund;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import au.gov.qld.pub.orders.ApplicationContextAwareTest;

public class RefundServiceIntegrationTest extends ApplicationContextAwareTest {
	@Autowired RefundService service;

	@Before
	public void setUp() throws Exception {
	}
	
	@Test
	public void shouldIgnorePreparingRefundsNotNew() {
	}
}
