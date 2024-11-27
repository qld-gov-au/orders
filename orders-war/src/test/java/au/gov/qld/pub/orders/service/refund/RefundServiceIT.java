package au.gov.qld.pub.orders.service.refund;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import au.gov.qld.pub.orders.ApplicationContextAwareTest;

public class RefundServiceIT extends ApplicationContextAwareTest {
	@Autowired RefundService service;

	@BeforeEach
	public void setUp() throws Exception {
	}

	@Test
	public void shouldIgnorePreparingRefundsNotNew() {
	}
}
