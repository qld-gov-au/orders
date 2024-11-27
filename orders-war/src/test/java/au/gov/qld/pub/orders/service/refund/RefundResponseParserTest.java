package au.gov.qld.pub.orders.service.refund;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import au.gov.qld.pub.orders.service.refund.dto.RefundQueryResponse;
import au.gov.qld.pub.orders.service.refund.dto.RefundRequestResponse;

public class RefundResponseParserTest {

	RefundResponseParser parser;

	@BeforeEach
	public void setUp() {
		parser = new RefundResponseParser();
	}

	@Test
	public void throwExceptionIfUnableToParseRefundQueryResponse() {
		assertThrows(IllegalArgumentException.class, () -> {
			parser.parseQueryResponse("bogus");
		});
	}

	@Test
	public void shouldReturnParsedQueryResponse() throws IOException {
		String refundQueryWsResponse = IOUtils.resourceToString("/testrefundqueryresponse.xml", StandardCharsets.UTF_8);
		RefundQueryResponse response = parser.parseQueryResponse(refundQueryWsResponse);
		assertThat(response.getLineItem().size(), is(2));
		assertThat(response.getLineItem().get(0).getOrderLineId(), is("order line 1"));
		assertThat(response.getLineItem().get(0).getQuatity(), is(1));
		assertThat(response.getLineItem().get(0).getPapiLineItemId(), is(258093));
		assertThat(response.getLineItem().get(0).getAgencyReference(), is("ccpb"));

		assertThat(response.getLineItem().get(1).getOrderLineId(), is("order line 2"));
		assertThat(response.getLineItem().get(1).getQuatity(), is(1));
		assertThat(response.getLineItem().get(1).getPapiLineItemId(), is(258092));
		assertThat(response.getLineItem().get(1).getAgencyReference(), is("aure"));
	}

	@Test
	public void shouldReturnParsedRequestResponse() throws IOException {
		String refundRequestWsResponse = IOUtils.resourceToString("/testrefundrequestresponse.xml", StandardCharsets.UTF_8);
		RefundRequestResponse response = parser.parseRequestResponse(refundRequestWsResponse);
		assertThat(response.getErrorMessage(), is("Transaction not found"));
	}
}
