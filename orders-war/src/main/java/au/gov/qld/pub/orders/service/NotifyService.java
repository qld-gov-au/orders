package au.gov.qld.pub.orders.service;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import au.gov.qld.pub.orders.dao.OrderDAO;
import au.gov.qld.pub.orders.entity.Item;
import au.gov.qld.pub.orders.entity.Order;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

@Service
public class NotifyService {
    private static final String EMAIL_DETAILS_FIELD_IN_ORDER = "email";
	private static final Logger LOG = LoggerFactory.getLogger(NotifyService.class);
   
    private final OrderDAO orderDAO;
    private final JavaMailSender mailSender;
    private final ConfigurationService configurationService;
    private final Configuration templateConfiguration;
    private final OrderGrouper orderGrouper;
    private final InlineTemplateService inlineTemplateService;
    private final AttachmentService attachmentService;
	private final AdditionalMailContentService additionalMailContentService;
	private final AdditionalNotificationService additionalNotificationService;
    
    @Autowired
    public NotifyService(ConfigurationService configurationService, OrderDAO orderDAO, JavaMailSender mailSender, 
            OrderGrouper orderGrouper, InlineTemplateService inlineTemplateService, AttachmentService attachmentService,
            AdditionalMailContentService additionalMailContentService, AdditionalNotificationService additionalNotificationService) {
        this.orderDAO = orderDAO;
        this.mailSender = mailSender;
        this.configurationService = configurationService;
        this.orderGrouper = orderGrouper;
        this.inlineTemplateService = inlineTemplateService;
        this.attachmentService = attachmentService;
		this.additionalMailContentService = additionalMailContentService;
		this.additionalNotificationService = additionalNotificationService;
        this.templateConfiguration = getTemplateConfiguration();
        this.templateConfiguration.setClassForTemplateLoading(getClass(), "/products/emails/");
    }

    protected Configuration getTemplateConfiguration() {
        return new Configuration();
    }

    @Transactional(rollbackFor = ServiceException.class)
    public void send(Order order) throws ServiceException {
        if (isNotBlank(order.getNotified())) {
            LOG.info("Notify for already notified order: {}", order.getId());
            return;
        }
        
        if (isBlank(order.getPaid())) {
            LOG.error("Attempted to notify unpaid order: {}", order.getId());
            return;
        }
        
        LOG.info("Notifying order: {}", order.getId());
        
        Map<String, Order> productIdOrders = orderGrouper.paidByProductGroup(order);
        try {
            for (Map.Entry<String, Order> productIdOrder : productIdOrders.entrySet()) {
                notifyOrderWithProductId(productIdOrder.getKey(), productIdOrder.getValue());
            }
        } catch (TemplateException | IOException | MessagingException | InterruptedException e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException(e);
        }
        
        additionalNotificationService.notifedPaidOrder(order.getId(), order.getCreated(), order.getPaid(), order.getReceipt(), order.getCartId(),
        		order.getCustomerDetailsMap(), order.getDeliveryDetailsMap(), toFieldMaps(order.getPaidItems()));
        Date notifiedAt = new Date();
        order.setNotified(new LocalDateTime(notifiedAt).toString());
        order.setNotifiedAt(notifiedAt);
        orderDAO.save(order);
        LOG.info("Notified order: {}", order.getId());
    }

	private void notifyOrderWithProductId(String productId, Order groupedOrder) throws TemplateException, IOException, MessagingException, InterruptedException {
        Collection<Item> distinctBusinessEmails = findDistinctEmailsInGroup(groupedOrder, NotifyType.BUSINESS);
        for (Item item : distinctBusinessEmails) {
            notifyOrder(productId, groupedOrder, item.getNotifyBusinessEmail(), item.getNotifyBusinessEmailSubject(), NotifyType.BUSINESS);
        }
        
        Collection<Item> distinctCustomerEmails = findDistinctEmailsInGroup(groupedOrder, NotifyType.CUSTOMER);
        for (Item item : distinctCustomerEmails) {
        	String customerEmailTo = getCustomerEmailTo(groupedOrder, item.getNotifyCustomerEmailField());
            notifyOrder(productId, groupedOrder, customerEmailTo, item.getNotifyCustomerEmailSubject(), NotifyType.CUSTOMER);
        }
    }

    private Collection<Item> findDistinctEmailsInGroup(Order groupedOrder, NotifyType type) {
    	Map<String, Item> distinct = new HashMap<>();
    	for (Item item : groupedOrder.getPaidItems()) {
    		String email = NotifyType.BUSINESS.equals(type) ? item.getNotifyBusinessEmail() : getCustomerEmailTo(groupedOrder, item.getNotifyCustomerEmailField());
    		String subject = NotifyType.BUSINESS.equals(type) ? item.getNotifyBusinessEmailSubject() : item.getNotifyCustomerEmailSubject();
    		if (isNotBlank(email) && isNotBlank(subject)) {
    			distinct.put(email, item);
    		}
    	}
		return distinct.values();
	}

	private String getCustomerEmailTo(Order order, String customerEmailField) {
        return "customerDetails".equals(customerEmailField) ? 
                order.getCustomerDetailsMap().get(EMAIL_DETAILS_FIELD_IN_ORDER) : order.getDeliveryDetailsMap().get(EMAIL_DETAILS_FIELD_IN_ORDER);
    }
    
    private void notifyOrder(String productId, Order groupedOrder, String to, String subject, NotifyType notifyType) 
        throws TemplateException, IOException, MessagingException, InterruptedException {
        if (isBlank(to)) {
            LOG.info("No {} email to notify for order receipt: {}", notifyType, groupedOrder.getReceipt());
            return;
        }
        
        List<EmailAttachment> attachments = attachmentService.retrieve(groupedOrder, notifyType);
        
        String emailBody = prepareTemplate(productId, groupedOrder, notifyType.name().toLowerCase(Locale.ENGLISH));
        LOG.info("Sending {} email to: {} for order receipt: {}", notifyType, to, groupedOrder.getReceipt());
        sendEmail(groupedOrder, to, subject, emailBody, attachments, NotifyType.CUSTOMER.equals(notifyType));
    }
    
    private void sendEmail(Order order, String to, String subject, String emailBody, List<EmailAttachment> attachments, boolean customerEmail) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, !attachments.isEmpty());
    
        helper.setTo(to);
        helper.setSubject(inlineTemplateService.template("subject", subject, order));
        helper.setText(emailBody);
        helper.setFrom(configurationService.getMailFrom());
        
        int attachmentCounter = 0;
        for (EmailAttachment attachment : attachments) {
            attachmentCounter++;
            helper.addAttachment(attachmentCounter + "-" + attachment.getName(), attachment.getData());
        }
        
        List<Map<String, String>> paidItemsFields = toFieldMaps(order.getPaidItems());
        additionalMailContentService.append(message, helper, customerEmail, paidItemsFields);
        mailSender.send(message);
    }

    private List<Map<String, String>> toFieldMaps(List<Item> items) {
    	List<Map<String, String>> paid = new ArrayList<>();
    	for (Item item : items) {
			paid.add(item.getFieldsMap());
    	}
		return paid;
	}

	private String prepareTemplate(String productId, Order order, String templateName) throws TemplateException, IOException {
        Template template = templateConfiguration.getTemplate(productId + "." + templateName + ".email.ftl");
        StringWriter out = new StringWriter();
        
        Map<String, Object> dataModel = new HashMap<String, Object>();
        dataModel.put("productId", productId);
        dataModel.put("order", order);
        template.process(dataModel, out);
        
        return out.toString();
    }
}
