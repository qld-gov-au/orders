package au.gov.qld.pub.orders.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ConfigurationService {
	@Value("${web.context}") 		private String context;
	@Value("${serviceFranchise}") 	private String serviceFranchise;
	@Value("${serviceName}") 		private String serviceName;
	@Value("${papiBase}")			private String papiBase;
	@Value("${sourceUrl}") 			private String sourceUrl;
	@Value("${serviceWsUsername}") 	private String serviceWsUsername;
	@Value("${serviceWsPassword}") 	private String serviceWsPassword;
	@Value("${serviceWsEndpoint}") 	private String serviceWsEndpoint;
	@Value("${serviceWsNotify}") 	private String serviceWsNotify;
	@Value("${serviceFullUrl}") 	private String serviceFullUrl;
	@Value("${errorRedirect}") 		private String errorRedirect;
	@Value("${mail.from}") 			private String mailFrom;

	public String getContext() {
		return context;
	}

	public String getServiceFranchise() {
		return serviceFranchise;
	}

	public String getServiceName() {
		return serviceName;
	}

	public String getServiceFullUrl() {
		return serviceFullUrl;
	}

	public String getPapiBase() {
		return papiBase;
	}
	
	public String getSourceUrl() {
		return sourceUrl;
	}	
	
	public String getServiceWsUsername() {
		return serviceWsUsername;
	}
	
	public String getServiceWsPassword() {
		return serviceWsPassword;
	}

	public String getServiceWsEndpoint() {
		return serviceWsEndpoint;
	}

	public String getErrorRedirect() {
		return errorRedirect;
	}
	
	public String getServiceWsNotify() {
		return serviceWsNotify;
	}

	public String getMailFrom() {
		return mailFrom;
	}
}
