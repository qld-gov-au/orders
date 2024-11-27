package au.gov.qld.pub.orders.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import au.gov.qld.pub.orders.entity.Item;
import au.gov.qld.pub.orders.entity.ItemBuilder;


public class InlineTemplateServiceTest {
    InlineTemplateService service;

    @BeforeEach
    public void setUp() {
        service = new InlineTemplateService();
    }

    @Test
    public void returnTemplatedString() {
        Item item = new ItemBuilder().build();
        String result = service.template("anything", "${productId}", item);
        assertThat(result, is(item.getProductId()));
    }

    @Test
    public void throwRuntimeExceptionOnException() {
        assertThrows(IllegalStateException.class, () -> {
            service.template("anything", "${productId}", null);
        });
    }
}
