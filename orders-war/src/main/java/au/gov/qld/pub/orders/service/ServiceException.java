package au.gov.qld.pub.orders.service;

public class ServiceException extends Exception {

    private static final long serialVersionUID = 1L;

    public ServiceException(Exception e) {
        super(e.getMessage(), e);
    }

    public ServiceException(String message) {
        super(message);
    }

}
