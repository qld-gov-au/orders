package au.gov.qld.pub.orders.web;

import java.util.Collection;
import java.util.Map;

import au.gov.qld.pub.orders.web.validator.OrderValidator;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import au.gov.qld.pub.orders.service.ValidationException;

@Component
public class StubOrderValidator extends OrderValidator {

	@Override
	public String getProductGroup() {
		return "stub";
	}

	@Override
	public void validate(Collection<MultipartFile> uploads, Map<String, String> fieldsAndValues) throws ValidationException {

	}

}
