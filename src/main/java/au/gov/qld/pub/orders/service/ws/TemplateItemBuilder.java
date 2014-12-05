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
        List<TemplateItem> templated = new ArrayList<TemplateItem>();
        for (Item item : items) {
            TemplateItem templateItem = new TemplateItem(inlineTemplateService);
            ReflectionUtils.shallowCopyFieldState(item, templateItem);
            templated.add(templateItem);
        }
        return templated;
    }

    static class TemplateItem extends Item {

        private final InlineTemplateService inlineTemplateService;

        public TemplateItem(InlineTemplateService inlineTemplateService) {
            super(null, null, null, null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null, null, null, null);
            this.inlineTemplateService = inlineTemplateService;
        }
        
        @Override
        public String getTitle() {
            return inlineTemplateService.template("title", super.getTitle(), this);
        }
        
        @Override
        public String getDescription() {
            return inlineTemplateService.template("description", super.getDescription(), this);
        }
    }

}
