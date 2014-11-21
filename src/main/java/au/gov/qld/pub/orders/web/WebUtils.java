package au.gov.qld.pub.orders.web;

import org.springframework.web.servlet.view.RedirectView;

public class WebUtils {

    private WebUtils() {
    }
    
    public static RedirectView redirect(String url) {
        RedirectView view = new RedirectView(url);
        view.setExposeModelAttributes(false);
        return view;
    }

}
