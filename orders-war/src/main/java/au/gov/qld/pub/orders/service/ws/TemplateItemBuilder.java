package au.gov.qld.pub.orders.service.ws;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import au.gov.qld.pub.orders.entity.Item;
import au.gov.qld.pub.orders.service.InlineTemplateService;

@Component
public class TemplateItemBuilder {
    private final InlineTemplateService inlineTemplateService;

    @Autowired
    public TemplateItemBuilder(InlineTemplateService inlineTemplateService) {
        this.inlineTemplateService = inlineTemplateService;
    }
    
    public List<TemplateItem> build(List<Item> items) {
        List<TemplateItem> templated = new ArrayList<>();
        for (Item item : items) {
            TemplateItem templateItem = new TemplateItem(inlineTemplateService);
            ReflectionUtils.shallowCopyFieldState(item, templateItem);
            templated.add(templateItem);
        }
        return templated;
    }

}
