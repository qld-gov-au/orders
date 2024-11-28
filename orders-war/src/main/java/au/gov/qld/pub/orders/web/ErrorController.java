package au.gov.qld.pub.orders.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class ErrorController implements org.springframework.boot.web.servlet.error.ErrorController {

    private static final Logger LOG = LoggerFactory.getLogger(ErrorController.class);

    protected static final String ERROR_403 = "error";
    protected static final String ERROR_404 = "error";
    protected static final String ERROR_5XX = "error";
    protected static final String GENERAL_ERROR_VIEW = "error";

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        LOG.info("Received error, status: {}", status);

        if (status != null) {
            int statusCode = Integer.valueOf(status.toString());
            if (statusCode == HttpStatus.FORBIDDEN.value()) {
                return ERROR_403;
            } else if (statusCode == HttpStatus.NOT_FOUND.value()) {
                return ERROR_404;
            } else if (statusCode >= HttpStatus.INTERNAL_SERVER_ERROR.value()
                && statusCode <= HttpStatus.NETWORK_AUTHENTICATION_REQUIRED.value()) {
                return ERROR_5XX;
            }
        }

        return GENERAL_ERROR_VIEW;
    }
}
