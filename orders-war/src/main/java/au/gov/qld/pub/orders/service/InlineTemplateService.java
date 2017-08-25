package au.gov.qld.pub.orders.service;

import java.io.StringReader;
import java.io.StringWriter;

import org.springframework.stereotype.Service;

import freemarker.template.Configuration;
import freemarker.template.Template;

@Service
public class InlineTemplateService {

    private final Configuration configuration;
    
    public InlineTemplateService() {
        this.configuration = new Configuration(Configuration.VERSION_2_3_23);
    }

    public String template(String nameOfTemplate, String template, Object templateData) {
        try {
            Template ftl = new Template(nameOfTemplate, new StringReader(template), configuration);
            StringWriter writer = new StringWriter();
            ftl.process(templateData, writer);
            return writer.toString();
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

}
