package au.gov.qld.pub.orders.service;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
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
import org.springframework.core.io.ByteArrayResource;
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
    private static final Logger LOG = LoggerFactory.getLogger(NotifyService.class);
   
    private final OrderDAO orderDAO;
    private final JavaMailSender mailSender;
    private final ConfigurationService configurationService;
    private final Configuration templateConfiguration;
    private final OrderGrouper orderGrouper;
    private final InlineTemplateService inlineTemplateService;
    private final AttachmentService attachmentService;
	private final AdditionalMailContentService additionalMailContentService;
    
    @Autowired
    public NotifyService(ConfigurationService configurationService, OrderDAO orderDAO, JavaMailSender mailSender, 
            OrderGrouper orderGrouper, InlineTemplateService inlineTemplateService, AttachmentService attachmentService,
            AdditionalMailContentService additionalMailContentService) {
        this.orderDAO = orderDAO;
        this.mailSender = mailSender;
        this.configurationService = configurationService;
        this.orderGrouper = orderGrouper;
        this.inlineTemplateService = inlineTemplateService;
        this.attachmentService = attachmentService;
		this.additionalMailContentService = additionalMailContentService;
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
        
        order.setNotified(new LocalDateTime().toString());
        orderDAO.save(order);
        LOG.info("Notified order: {}", order.getId());
    }

	private void notifyOrderWithProductId(String productId, Order groupedOrder) throws TemplateException, IOException, MessagingException, InterruptedException {
        Item first = groupedOrder.getPaidItems().get(0);
        if (isNotBlank(first.getNotifyBusinessEmail())) {
            notifyOrder(productId, groupedOrder, first.getNotifyBusinessEmail(), first.getNotifyBusinessEmailSubject(), 
            		first.getNotifyBusinessFormFilename(), NotifyType.BUSINESS);
        }
        
        String customerEmailField = first.getNotifyCustomerEmailField();
        if (isNotBlank(customerEmailField)) {
            String customerEmailTo = getCustomerEmailTo(groupedOrder, customerEmailField);
            notifyOrder(productId, groupedOrder, customerEmailTo, first.getNotifyCustomerEmailSubject(), 
            		first.getNotifyCustomerFormFilename(), NotifyType.CUSTOMER);
        }
    }

    private String getCustomerEmailTo(Order order, String customerEmailField) {
        return "customerDetails".equals(customerEmailField) ? 
                order.getCustomerDetailsMap().get("email") : order.getDeliveryDetailsMap().get("email");
    }
    
    private void notifyOrder(String productId, Order groupedOrder, String to, String subject, String filename, NotifyType notifyType) 
        throws TemplateException, IOException, MessagingException, InterruptedException {
        if (isBlank(to)) {
            LOG.info("No {} email to notify for order receipt: {}", notifyType, groupedOrder.getReceipt());
            return;
        }
        
        List<byte[]> attachments = attachmentService.retrieve(groupedOrder, notifyType);
        
        String emailBody = prepareTemplate(productId, groupedOrder, notifyType.name().toLowerCase(Locale.ENGLISH));
        LOG.info("Sending {} email to: {} for order receipt: {}", notifyType, to, groupedOrder.getReceipt());
        sendEmail(groupedOrder, to, subject, emailBody, filename, attachments, NotifyType.CUSTOMER.equals(notifyType));
    }
    
    private void sendEmail(Order order, String to, String subject, String emailBody, String filename, List<byte[]> attachments, boolean customerEmail) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, !attachments.isEmpty());
    
        helper.setTo(to);
        helper.setSubject(inlineTemplateService.template("subject", subject, order));
        helper.setText(emailBody);
        helper.setFrom(configurationService.getMailFrom());
        
        int attachmentCounter = 0;
        for (byte[] attachment : attachments) {
            attachmentCounter++;
            helper.addAttachment(attachmentCounter + "-" + filename, new ByteArrayResource(attachment));
        }
        
        List<Map<String, String>> paidItemsFields = filterPaidItemsFields(order.getPaidItems());
        additionalMailContentService.append(message, helper, customerEmail, paidItemsFields);
        mailSender.send(message);
    }

    private List<Map<String, String>> filterPaidItemsFields(List<Item> items) {
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
