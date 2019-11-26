package au.gov.qld.pub.orders.web;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;

import au.gov.qld.pub.orders.service.refund.RefundService;

@RunWith(MockitoJUnitRunner.class)
public class RefundControllerTest {
	
	@Mock RefundService refundService;
	
	RefundController controller;

	@Before
	public void setUp() {
		controller = new RefundController(refundService);
	}
	
	@Test
	public void shouldStartRefundAndReturnOkIfNoErrors() {
		assertThat(controller.refundNew().getStatusCode(), is(HttpStatus.OK));
		verify(refundService).refundNewItems();
	}
	
	@Test
	public void shouldStartRefundAndReturnServerErrorIfErrors() {
		doThrow(new RuntimeException("expected")).when(refundService).refundNewItems();
		assertThat(controller.refundNew().getStatusCode(), is(HttpStatus.INTERNAL_SERVER_ERROR));
		verify(refundService).refundNewItems();
	}
}
