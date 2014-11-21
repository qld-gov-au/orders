package au.gov.qld.bdm.orders.service.ws;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Before;
import org.junit.Test;


public class CartResponseParserTest {
	static final String ORDER_STATUS = "<OrderStatusResponse xmlns=\"http://smartservice.qld.gov.au/payment/schemas/payment_api_1_3\">"
			+ "<status>PAID</status><receiptNumber>1831572</receiptNumber></OrderStatusResponse>";
	static final String UNPAID_ORDER_STATUS = "<OrderStatusResponse xmlns=\"http://smartservice.qld.gov.au/payment/schemas/payment_api_1_3\">"
			+ "<status>NOT_PAID</status></OrderStatusResponse>";
	
	static final String ORDER_QUERY = "<OrderQueryResponse xmlns=\"http://smartservice.qld.gov.au/payment/schemas/payment_api_1_3\">"
			+ "<customerDetail type=\"Customer\">"
			+ "<detail type=\"nameTitle\"/>"
			+ "<detail type=\"givenName\">some givenName</detail>"
			+ "<detail type=\"familyName\">some familyName</detail>"
			+ "<detail type=\"organisationName\"/>"
			+ "</customerDetail>"
			+ "<customerDetail type=\"Email\">"
			+ "<detail type=\"email\">some email</detail>"
			+ "</customerDetail>"
			+ "<customerDetail type=\"Phone\">"
			+ "<detail type=\"phone\">some phone</detail>"
			+ "</customerDetail>"
			+ "<deliveryAddress type=\"Post\">"
			+ "<detail type=\"fullName\">asdasd@asd</detail>"
			+ "<detail type=\"line1\">asasd</detail>"
			+ "<detail type=\"line2\"/>"
			+ "<detail type=\"line3\"/>"
			+ "<detail type=\"localityName\">asdasd@asd</detail>"
			+ "<detail type=\"postcode\"/>"
			+ "<detail type=\"stateTerritory\">QLD</detail>"
			+ "<detail type=\"countryName\">Australia</detail>"
			+ "</deliveryAddress>"
			+ "<order>"
			+ "<onlineService id=\"test\" name=\"BDM orders\" next=\"http://localhost:8091/orders/test\" notify=\"http://localhost:8091/orders/notify/6a792941-2c9d-46f1-8c6c-ba53366af47c\" prev=\"http://localhost:8091/orders/test\"/>"
			+ "<orderline id=\"some orderline id\" quantity=\"2\">"
			+ "<product agency=\"test agency\" cost=\"45\" description=\"test description\" disbursementId=\"999\" gst=\"123\" ref=\"test reference\" title=\"test title\"><accounting costCenter=\"cc\" glCode=\"gl\" narrative=\"narrative\" taxCode=\"gl\"/></product>"
			+ "<deliveryAddressRequest type=\"POST\"/>"
			+ "<customerDetailsRequest type=\"CUSTOMER\"/>"
			+ "<customerDetailsRequest type=\"PHONE\"/>"
			+ "<customerDetailsRequest type=\"EMAIL\"/><"
			+ "/orderline>"
			+ "</order>"
			+ "</OrderQueryResponse>";

	CartResponseParser parser;
	
	@Before
	public void setUp() {
		parser = new CartResponseParser();
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void throwExceptionWhenInvalidStatusXml() {
		parser.getReceipt("bogus");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void throwExceptionWhenInvalidQueryXml() {
		parser.getPaidOrderDetails("bogus");
	}
	
	@Test
	public void receiptFromStatus() {
		String receipt = parser.getReceipt(ORDER_STATUS);
		assertThat(receipt, is("1831572"));
	}
	
	@Test
	public void nullReceiptFromUnpaidStatus() {
		String receipt = parser.getReceipt(UNPAID_ORDER_STATUS);
		assertThat(receipt, nullValue());
	}
	
	@Test
	public void detailsFromQuery() {
		OrderDetails details = parser.getPaidOrderDetails(ORDER_QUERY);
		assertThat(details, notNullValue());
		assertThat(details.getOrderlineQuantities().get("some orderline id"), is("2"));
		assertThat(details.getDeliveryDetails().get("stateTerritory"), is("QLD"));
		assertThat(details.getCustomerDetails().get("givenName"), is("some givenName"));
		assertThat(details.getCustomerDetails().get("email"), is("some email"));
		assertThat(details.getCustomerDetails().get("phone"), is("some phone"));
	}
}
