package au.gov.qld.pub.orders.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/orders")
public class DefaultControlerStuff {


    @RequestMapping("/")
    public String randohome() {
        return "error";
    }

    @RequestMapping("/notfound")
        public String notfound() {
        return "notfound";
    }
    @RequestMapping("/added")
        public String added() {
        return "added";
    }
    @RequestMapping("/test")
        public String test() {
        return "test";
    }

}
