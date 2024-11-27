package au.gov.qld.pub.orders.service.ws;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.util.ReflectionUtils;

import au.gov.qld.pub.orders.entity.Item;
import au.gov.qld.pub.orders.service.InlineTemplateService;
import au.gov.qld.pub.orders.service.ItemPropertiesDTO;

public class TemplateItemTest {
	@Test
    public void templatedTitleAndDescriptionWithTruncatedAttributes() {
        TemplateItem templateItem = new TemplateItem(new InlineTemplateService());
        ItemPropertiesDTO dto = new ItemPropertiesDTO();
        dto.setTitle("${description}");
        dto.setDescription(StringUtils.repeat("a", TemplateItem.MAX_DESCRIPTION_LENGTH + 1));
		Item item = Item.createItem(dto);
        ReflectionUtils.shallowCopyFieldState(item, templateItem);
        
        assertThat(templateItem.getTitle(), is(StringUtils.repeat("a", TemplateItem.MAX_TITLE_LENGTH - TemplateItem.TAIL.length()) + TemplateItem.TAIL));
        assertThat(templateItem.getDescription(), is(StringUtils.repeat("a", TemplateItem.MAX_DESCRIPTION_LENGTH - TemplateItem.TAIL.length()) + TemplateItem.TAIL));
    }
	
	@Test
    public void templatedTitleFromDescription() {
        TemplateItem templateItem = new TemplateItem(new InlineTemplateService());
        ItemPropertiesDTO dto = new ItemPropertiesDTO();
        dto.setTitle("${description}");
        dto.setDescription("description");
		Item item = Item.createItem(dto);
        ReflectionUtils.shallowCopyFieldState(item, templateItem);
        
        assertThat(templateItem.getTitle(), is("description"));
        assertThat(templateItem.getDescription(), is("description"));
    }
	
	@Test
    public void templatedDescriptionFromTitle() {
        TemplateItem templateItem = new TemplateItem(new InlineTemplateService());
        ItemPropertiesDTO dto = new ItemPropertiesDTO();
        dto.setTitle("title");
        dto.setDescription("${title}");
		Item item = Item.createItem(dto);
        ReflectionUtils.shallowCopyFieldState(item, templateItem);
        
        assertThat(templateItem.getTitle(), is("title"));
        assertThat(templateItem.getDescription(), is("title"));
    }
	
    @Test
    public void truncateTemplatedTitleAndDescription() {
        TemplateItem templateItem = new TemplateItem(new InlineTemplateService());
        ItemPropertiesDTO dto = new ItemPropertiesDTO();
        dto.setTitle(StringUtils.repeat("a", TemplateItem.MAX_TITLE_LENGTH + 1));
        dto.setDescription(StringUtils.repeat("a", TemplateItem.MAX_DESCRIPTION_LENGTH + 1));
		Item item = Item.createItem(dto);
        ReflectionUtils.shallowCopyFieldState(item, templateItem);
        
        assertThat(templateItem.getTitle(), is(StringUtils.repeat("a", TemplateItem.MAX_TITLE_LENGTH - TemplateItem.TAIL.length()) + TemplateItem.TAIL));
        assertThat(templateItem.getDescription(), is(StringUtils.repeat("a", TemplateItem.MAX_DESCRIPTION_LENGTH - TemplateItem.TAIL.length()) + TemplateItem.TAIL));
    }
}
