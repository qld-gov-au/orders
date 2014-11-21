package au.gov.qld.pub.orders.service.ws;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.gov.qld.pub.orders.entity.Order;
import au.gov.qld.pub.orders.service.ConfigurationService;
import au.gov.qld.pub.orders.service.ServiceException;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

@Component
public class RequestBuilder {

	private final Template requestTemplate;
	private final ConfigurationService configurationService;
	private final TemplateItemBuilder templateItemBuilder;

	@Autowired
	public RequestBuilder(ConfigurationService configurationService, TemplateItemBuilder templateItemBuilder) throws IOException {
		this.templateItemBuilder = templateItemBuilder;
		Configuration configuration = new Configuration();
		configuration.setClassForTemplateLoading(getClass(), "/templates");
    	this.requestTemplate = configuration.getTemplate("sc-request.xml");
    	this.configurationService = configurationService;
	}
	
	public String addRequest(Order order) throws ServiceException {
		StringWriter writer = new StringWriter();
		Map<String, Object> dataModel = new HashMap<String, Object>();
		dataModel.put("order", order);
		dataModel.put("templateItems", templateItemBuilder.build(order.getItems()));
		dataModel.put("config", configurationService);
		
		try {
			requestTemplate.process(dataModel, writer);
			writer.close();
			return writer.toString();
		} catch (TemplateException | IOException e) {
			throw new ServiceException(e);
		}
	}

}
