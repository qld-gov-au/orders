package au.gov.qld.pub.orders.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ConfigurationService {
    @Value("${web.context}") private String context;
    @Value("${serviceFranchise}") private String serviceFranchise;
    @Value("${serviceName}") private String serviceName;
    @Value("${papiBase}") private String papiBase;
    @Value("${sourceUrl}") private String sourceUrl;
    @Value("${serviceWsUsername}") private String serviceWsUsername;
    @Value("${serviceWsPassword}") private String serviceWsPassword;
    @Value("${serviceWsEndpoint}") private String serviceWsEndpoint;
    @Value("${serviceWsNotify}") private String serviceWsNotify;
    @Value("${serviceFullUrl}") private String serviceFullUrl;
    @Value("${errorRedirect}") private String errorRedirect;
    @Value("${mail.from}") private String mailFrom;
    @Value("${notifyFormRetryCount}") private Integer notifyFormRetryCount;
    @Value("${notifyFormRetryWait}") private Integer notifyFormRetryWait;
    @Value("${notifyFormTimeout}") private Integer notifyFormTimeout;
    @Value("${scheduler.statusCheck.maxAgeForRetry}") private Integer maxAgeForRetry;
    @Value("${noticeToPay.default.redirect}") private String noticeToPayDefaultRedirect;
    @Value("${noticeToPay.source.pattern}") private String noticeToPaySourcePattern;
    @Value("${noticeToPay.id.pattern}") private String noticeToPayIdPattern;
    @Value("${noticeToPay.notifyUrl}") private String noticeToPayNotifyUrl;
    @Value("${noticeToPay.disbursementId}") private String noticeToPayDisbursementId;

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

    public int getNotifyFormRetryCount() {
        return notifyFormRetryCount;
    }
    
    public int getNotifyFormRetryWait() {
        return notifyFormRetryWait;
    }
    
    public int getNotifyFormTimeout() {
        return notifyFormTimeout;
    }

    public int getMaxAgeForRetry() {
        return maxAgeForRetry;
    }

	public String getNoticeToPayDefaultRedirect() {
		return noticeToPayDefaultRedirect;
	}

	public String getNoticeToPaySourcePattern() {
		return noticeToPaySourcePattern;
	}

	public String getNoticeToPayIdPattern() {
		return noticeToPayIdPattern;
	}

	public String getNoticeToPayNotifyUrl() {
		return noticeToPayNotifyUrl;
	}

	public String getNoticeToPayDisbursementId() {
		return noticeToPayDisbursementId;
	}
}
