package au.gov.qld.pub.orders.service.refund;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Before;
import org.junit.Test;

import au.gov.qld.pub.orders.entity.RefundItem;

public class RefundRequestBuilderTest {
	
	private static final String RECEIPT = "some receipt";
	RefundRequestBuilder builder;
	RefundItem refundItem;

	@Before
	public void setUp() {
		refundItem = new RefundItem();
		refundItem.setPapiReceiptNumber(RECEIPT);
		builder = new RefundRequestBuilder();
	}
	
	@Test
	public void shouldBuildQueryRequest() {
		String request = builder.buildQuery(refundItem);
		assertThat(request, containsString("<RefundQueryRequest><papiReceiptNumber>" + RECEIPT + "</papiReceiptNumber></RefundQueryRequest>"));
	}
}
