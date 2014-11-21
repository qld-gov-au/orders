package au.gov.qld.bdm.orders.web;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import au.gov.qld.bdm.orders.service.ConfigurationService;

public class ViewPostProcessor extends HandlerInterceptorAdapter {
	private final ConfigurationService configurationService;

	@Autowired
	public ViewPostProcessor(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}
	
	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView mav) throws Exception {
		if (mav == null) {
			return;
		}
		
		addCartId(request, mav);
		mav.getModelMap().addAttribute("context", configurationService.getContext());
		mav.getModelMap().addAttribute("serviceFranchise", configurationService.getServiceFranchise());
		mav.getModelMap().addAttribute("serviceName", configurationService.getServiceName());
		mav.getModelMap().addAttribute("papiBase", configurationService.getPapiBase());
		mav.getModelMap().addAttribute("sourceUrl", configurationService.getSourceUrl());
	}

	private void addCartId(HttpServletRequest request, ModelAndView mav) {
		if (request.getCookies() == null) {
			return;
		}
		
		for (Cookie cookie : request.getCookies()) {
			if (Constants.CART_ID.equals(cookie.getName())) {
				mav.getModelMap().addAttribute(Constants.CART_ID, cookie.getValue());
				break;
			}
		}
	}
}
