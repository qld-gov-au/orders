package au.gov.qld.pub.orders.service;

import java.util.List;
import java.util.Map;

import jakarta.mail.internet.MimeMessage;

import org.springframework.mail.javamail.MimeMessageHelper;

public class StubAdditionalMailContentService implements AdditionalMailContentService {

	@Override
	public void append(MimeMessage message, MimeMessageHelper helper, boolean customerEmail, List<Map<String, String>> paidItemsFields) {
	}

}
