package au.gov.qld.pub.orders.service;

import java.util.Map;

public interface PreCartValidator {

	void validate(String productGroup, String productId, Map<String, String> fields);

}
