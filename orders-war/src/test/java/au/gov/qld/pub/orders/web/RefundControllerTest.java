package au.gov.qld.pub.orders.web;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import au.gov.qld.pub.orders.service.refund.RefundService;

@ExtendWith(MockitoExtension.class)
public class RefundControllerTest {
	
	@Mock RefundService refundService;
	
	RefundController controller;

	@BeforeEach
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
