package au.gov.qld.pub.orders.service;


public class StubPaymentInformationService implements PaymentInformationService {

	@Override
	public PaymentInformation fetch(String sourceId) {
		return new PaymentInformation(sourceId, "description", 123l, 45l);
	}

}
