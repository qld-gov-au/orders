package au.gov.qld.bdm.orders.web.ftl;

import freemarker.cache.TemplateLoader;  
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;  
  
import java.util.List;  
  
public class HtmlFreeMarkerConfigurer extends FreeMarkerConfigurer {  
  
    @Override  
    protected TemplateLoader getAggregateTemplateLoader(List<TemplateLoader> templateLoaders) {  
        return new HtmlTemplateLoader(super.getAggregateTemplateLoader(templateLoaders));  
    }  
}