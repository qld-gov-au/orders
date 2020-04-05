package au.gov.qld.pub.orders.service;

public interface FileValidator {
	void validate(String filename, long filesize) throws ValidationException;
}
