package au.gov.qld.pub.orders.config;


import au.gov.qld.pub.orders.config.filter.CspInterceptor;
import au.gov.qld.pub.orders.web.ViewPostProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;

import java.util.concurrent.TimeUnit;


@Configuration
public class ConfigMvc implements WebMvcConfigurer {

    @Autowired
    ViewPostProcessor viewPostProcessor;

    @Autowired
    private CspInterceptor cspInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(viewPostProcessor);
        registry.addInterceptor(cspInterceptor);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/web-template-release/**").addResourceLocations("classpath:/templates/web-template-release")
            .setCacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES));

//        registry.addResourceHandler("/resources/**").addResourceLocations("/resources/");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/error").setViewName("error");
        registry.addViewController("/notfound").setViewName("notfound");
        registry.addViewController("/added").setViewName("added");
        //- TODO: REMOVE FOR RELEASES or make only on profile dev local etc
        registry.addViewController("/test").setViewName("test");
    }




}
