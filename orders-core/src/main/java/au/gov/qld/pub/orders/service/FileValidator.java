package au.gov.qld.pub.orders.service;

import java.io.InputStream;

public interface FileValidator {
	void validate(String filename, long filesize, InputStream inputStream) throws ValidationException;
}
