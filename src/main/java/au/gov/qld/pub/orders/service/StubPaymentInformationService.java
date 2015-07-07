package au.gov.qld.pub.orders.service;


public class StubPaymentInformationService implements PaymentInformationService {

	public static final PaymentInformation STUB = new PaymentInformation("reference", "description", 123l, 45l);

	@Override
	public PaymentInformation fetch(String id) {
		return STUB;
	}

}
