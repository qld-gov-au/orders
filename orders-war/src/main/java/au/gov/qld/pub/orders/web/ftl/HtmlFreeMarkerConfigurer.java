package au.gov.qld.pub.orders.web.ftl;

import java.util.List;

import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import freemarker.cache.NullCacheStorage;
import freemarker.cache.TemplateLoader;  
  
public class HtmlFreeMarkerConfigurer extends FreeMarkerConfigurer {  
  
    @Override  
    protected TemplateLoader getAggregateTemplateLoader(List<TemplateLoader> templateLoaders) {  
    	getConfiguration().setCacheStorage(new NullCacheStorage());
        return new HtmlTemplateLoader(super.getAggregateTemplateLoader(templateLoaders));  
    }  
}