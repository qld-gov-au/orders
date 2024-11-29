package au.gov.qld.pub.orders.service;

import java.util.List;
import java.util.Map;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.springframework.mail.javamail.MimeMessageHelper;

public interface AdditionalMailContentService {

	void append(MimeMessage message, MimeMessageHelper helper, boolean customerEmail, List<Map<String, String>> paidItemsFields) throws MessagingException;

}
