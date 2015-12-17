package au.gov.qld.pub.orders.web;

import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.EnumerationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import au.gov.qld.pub.orders.ProductProperties;
import au.gov.qld.pub.orders.entity.Item;
import au.gov.qld.pub.orders.entity.Order;
import au.gov.qld.pub.orders.service.ConfigurationService;
import au.gov.qld.pub.orders.service.OrderService;
import au.gov.qld.pub.orders.service.ServiceException;

@Controller
public class OrderController {
    private static final Logger LOG = LoggerFactory.getLogger(OrderController.class);
    private static final int MAX_FIELDS = 100;
    private static final int MAX_FIELD_LENGTH = 2000;

    private final OrderService orderService;
    private final String errorRedirect;
    private final ConfigurationService configurationService;

    @Autowired
    public OrderController(OrderService orderService, ConfigurationService configurationService) {
        this.orderService = orderService;
        this.configurationService = configurationService;
        this.errorRedirect = configurationService.getErrorRedirect();
    }
    
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/confirm", method = RequestMethod.POST)
    public ModelAndView confirm(@RequestParam String group, HttpServletRequest request) {
        Map<String, Object> fields = new HashMap<String, Object>();
        List<String> fieldNames = EnumerationUtils.toList(request.getParameterNames());
        for (String fieldName : fieldNames) {
            if (isNotBlank(request.getParameter(fieldName))) {
                fields.put(fieldName, request.getParameter(fieldName));
            }
        }
        
        ModelAndView mav = new ModelAndView("confirm." + group);
        mav.getModel().put("fields", fields);
        return mav;
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> validateAndGetFields(HttpServletRequest request, Collection<String> allowedFields) {
        Map<String, String> fields = new HashMap<String, String>();
        Enumeration<String> parameterNames = (Enumeration<String>)request.getParameterNames();
        for (int i=0; i < MAX_FIELDS && parameterNames.hasMoreElements(); i++) {
            String name = (String)parameterNames.nextElement();
            if (!allowedFields.contains(name)) {
                continue;
            }
            
            String value = defaultString(request.getParameter(name)).trim();
            if (value.length() > MAX_FIELD_LENGTH) {
                value = value.substring(0, MAX_FIELD_LENGTH);
            }
            
            fields.put(name.trim(), value);
        }
        
        fields.remove("ssqCartId");
        fields.remove("productId");
        return fields;
    }

    private List<Item> validateAndCreate(ItemCommand command) {
        if (command.getProductId() == null) {
            return Collections.emptyList();
        }

        List<Item> items = new ArrayList<Item>();
        for (String productId : command.getProductId()) {
            Item item = orderService.findAndPopulate(productId);
            if (item != null) {
                items.add(item);
            }
        }
        
        return items;
    }
    
    @RequestMapping(value = "add", method = RequestMethod.POST)
    public RedirectView add(@CookieValue(value=Constants.CART_ID, required=false) String cookieCartId, @RequestParam(required=false) String ssqCartId, 
            @ModelAttribute("command") ItemCommand command, HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        String effectiveCartId = isBlank(cookieCartId) ? ssqCartId : cookieCartId;
        
        List<Item> items = validateAndCreate(command);
        if (items.isEmpty()) {
            return WebUtils.redirect(errorRedirect);
        }
        
        for (Item item : items) {
            Collection<String> allowedFields = orderService.getAllowedFields(item.getProductId(), ProductProperties.ACCEPT_FIELDS);
            item.setFields(validateAndGetFields(request, allowedFields));
        }
        
        LOG.info("Adding to cart with cartId: " + effectiveCartId);
        Order order = orderService.add(items, effectiveCartId);
        Cookie cookie = new Cookie(Constants.CART_ID, order.getCartId());
        cookie.setSecure(true);
        response.addCookie(cookie);
        return WebUtils.redirect(configurationService.getServiceFullUrl() + "/added");
    }
    
    
}
