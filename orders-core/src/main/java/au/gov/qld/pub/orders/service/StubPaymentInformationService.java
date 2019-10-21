package au.gov.qld.pub.orders.service;

import static java.util.Arrays.asList;

public class StubPaymentInformationService implements PaymentInformationService {

    @Override
    public PaymentInformation fetch(String sourceId) {
        return new PaymentInformation(sourceId, "description", asList(new OrderInformation("product", 123l, 45l, 1)), new Applicant("name", "addr1", "suburb", "state", "postcode", "country"));
    }

}
