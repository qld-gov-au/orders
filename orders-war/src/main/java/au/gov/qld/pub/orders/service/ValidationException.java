package au.gov.qld.pub.orders.service;

public class ValidationException extends Exception {
	private static final long serialVersionUID = 1L;

	public ValidationException(String msg) {
		super(msg);
	}

}
