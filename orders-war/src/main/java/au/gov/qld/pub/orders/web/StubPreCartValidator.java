package au.gov.qld.pub.orders.web;

import java.util.Map;

import org.springframework.stereotype.Component;

import au.gov.qld.pub.orders.service.PreCartValidator;

@Component
public class StubPreCartValidator implements PreCartValidator {

	@Override
	public void validate(String productGroup, String productId, Map<String, String> fields) {
		// no op
	}

}
