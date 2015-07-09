package au.gov.qld.pub.orders.service.ws;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.gov.qld.pub.orders.entity.Order;
import au.gov.qld.pub.orders.service.ConfigurationService;
import au.gov.qld.pub.orders.service.PaymentInformation;
import au.gov.qld.pub.orders.service.ServiceException;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

@Component
public class RequestBuilder {
    private static final String NTP_QUERY_TEMPLATE = 
        "<PaymentQueryRequest xmlns=\"http://smartservice.qld.gov.au/payment/schemas/payment_query_1_0\">"
        + "<paymentRequestId>%s</paymentRequestId>"
        +"</PaymentQueryRequest>";

    private final Template shoppingCartRequestTemplate;
    private final ConfigurationService configurationService;
    private final TemplateItemBuilder templateItemBuilder;
    private final Template noticeToPayRequestTemplate;

    @Autowired
    public RequestBuilder(ConfigurationService configurationService, TemplateItemBuilder templateItemBuilder) throws IOException {
        this.templateItemBuilder = templateItemBuilder;
        Configuration configuration = new Configuration();
        configuration.setClassForTemplateLoading(getClass(), "/templates");
        this.shoppingCartRequestTemplate = configuration.getTemplate("sc-request.xml");
        this.noticeToPayRequestTemplate = configuration.getTemplate("ntp-request.xml");
        this.configurationService = configurationService;
    }
    
    public String addRequest(Order order) throws ServiceException {
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("order", order);
        dataModel.put("templateItems", templateItemBuilder.build(order.getItems()));
        dataModel.put("config", configurationService);
        return processTemplate(shoppingCartRequestTemplate, dataModel);
    }

    private String processTemplate(Template template, Map<String, Object> dataModel) throws ServiceException {
        try {
            StringWriter writer = new StringWriter();
            template.process(dataModel, writer);
            writer.close();
            return writer.toString();
        } catch (TemplateException | IOException e) {
            throw new ServiceException(e);
        }
    }

    public String noticeToPay(PaymentInformation paymentInformation, String noticeToPayId, String sourceUrl) throws ServiceException {
        Map<String, Object> dataModel = new HashMap<>();
        // Payment gateway only allows 8-10 character IDs
        dataModel.put("paymentRequestId", noticeToPayId.substring(noticeToPayId.length() - 10));
        dataModel.put("paymentInformation", paymentInformation);
        dataModel.put("config", configurationService);
        dataModel.put("noticeToPayId", noticeToPayId);
        dataModel.put("sourceUrl", sourceUrl);
        return processTemplate(noticeToPayRequestTemplate, dataModel);
    }
    
    public String noticeToPayQuery(String noticeToPayId) {
        String paymentRequestId = noticeToPayId.substring(noticeToPayId.length() - 10);
        return String.format(NTP_QUERY_TEMPLATE, paymentRequestId);
    }

}
