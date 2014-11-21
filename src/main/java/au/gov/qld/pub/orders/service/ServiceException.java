package au.gov.qld.bdm.orders.service;

public class ServiceException extends Exception {

	private static final long serialVersionUID = 1L;

	public ServiceException(Exception e) {
		super(e.getMessage(), e);
	}

}
