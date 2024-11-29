package au.gov.qld.pub.orders.web;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import au.gov.qld.pub.orders.ApplicationContextAwareTest;

public class OrderControllerIT extends ApplicationContextAwareTest {
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void confirmAndEscapeParams() throws Exception {
        mockMvc.perform(post("/confirm").param("group", "testgroup").param("field", "<xml>value</xml>")).andExpect(
                content().string(containsString("&lt;xml&gt;value&lt;/xml&gt;")));


    }

}
