package au.gov.qld.pub.orders.web;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.Collection;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import au.gov.qld.pub.orders.service.ValidationException;

public abstract class OrderValidator {

	public abstract String getProductGroup();

	public abstract void validate(Collection<MultipartFile> uploads, Map<String, String> fieldsAndValues) throws ValidationException;

	protected final void rejectIfEmpty(String fieldName, Map<String, String> fieldsAndValues) throws ValidationException {
    	if (isBlank(fieldsAndValues.get(fieldName))) {
    		throw new ValidationException("Missing value for " + fieldName);
    	}
	}
	
	protected final void rejectIfAllEmpty(Collection<MultipartFile> uploads) throws ValidationException {
		for (MultipartFile upload : uploads) {
			if (upload != null && !upload.isEmpty()) {
				return;
			}
		}
		throw new ValidationException("Missing an upload");
	}
	
	protected final void rejectIfTooLong(String fieldName, Map<String, String> fieldsAndValues) throws ValidationException {
    	if (fieldsAndValues.get(fieldName).length() >= 600) {
    		throw new ValidationException("String is too long for " + fieldName);
    	}
	}

}