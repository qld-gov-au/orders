package au.gov.qld.pub.orders.service.ws;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import au.gov.qld.pub.orders.service.ConfigurationService;
import au.gov.qld.pub.orders.service.ServiceException;

@Service
public class CartService {
    private static final String NS = "http://smartservice.qld.gov.au/payment/schemas/shopping_cart_1_3";
    private static final String PAYMENT_API_NS = "http://smartservice.qld.gov.au/payment/schemas/payment_api_1_3";
    private static final String QUERY_TEMPLATE = "<OrderStatusRequest><generatedOrderId>@ORDER_ID@</generatedOrderId></OrderStatusRequest>";
    private static final String ORDER_QUERY_TEMPLATE = "<OrderQueryRequest><generatedOrderId>@ORDER_ID@</generatedOrderId></OrderQueryRequest>";

    private final String username;
    private final SOAPClient client;
    private final byte[] passwordBytes;
    
    @Autowired
    public CartService(ConfigurationService configurationService) throws IOException {
        String password = configurationService.getServiceWsPassword();
        String endpoint = configurationService.getServiceWsEndpoint();
        this.username = configurationService.getServiceWsUsername();
        this.passwordBytes = password.getBytes("UTF-8");
        this.client = new SOAPClient(endpoint);
    }
    
    public String addToCart(String request) throws ServiceException {
        return client.sendRequest(username, passwordBytes, NS, request);
    }

    public String orderStatus(String generatedId) throws ServiceException {
        String request = QUERY_TEMPLATE.replace("@ORDER_ID@", generatedId);
        return client.sendRequest(username, passwordBytes, PAYMENT_API_NS, request);
    }
    
    public String orderQuery(String generatedId) throws ServiceException {
        String request = ORDER_QUERY_TEMPLATE.replace("@ORDER_ID@", generatedId);
        return client.sendRequest(username, passwordBytes, PAYMENT_API_NS, request);
    }
}
