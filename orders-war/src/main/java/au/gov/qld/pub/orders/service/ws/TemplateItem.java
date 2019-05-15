package au.gov.qld.pub.orders.service.ws;

import au.gov.qld.pub.orders.entity.Item;
import au.gov.qld.pub.orders.service.InlineTemplateService;

public class TemplateItem extends Item {

    static final int MAX_DESCRIPTION_LENGTH = 300;
	static final int MAX_TITLE_LENGTH = 100;
    static final String TAIL = "...";
    private final InlineTemplateService inlineTemplateService;
    
    public TemplateItem(InlineTemplateService inlineTemplateService) {
        super();
        this.inlineTemplateService = inlineTemplateService;
    }
    
    @Override
    public String getTitle() {
        return truncate(inlineTemplateService.template("title", super.getTitle(), this), MAX_TITLE_LENGTH);
    }
    
    private String truncate(String templated, int maxLength) {
		return templated.length() > maxLength ? templated.substring(0, maxLength - TAIL.length()) + TAIL : templated;
	}

	@Override
    public String getDescription() {
        return truncate(inlineTemplateService.template("description", super.getDescription(), this), MAX_DESCRIPTION_LENGTH);
    }
}