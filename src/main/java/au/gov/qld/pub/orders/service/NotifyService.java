package au.gov.qld.pub.orders.service;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
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
    
    @Autowired
    public NotifyService(ConfigurationService configurationService, OrderDAO orderDAO, JavaMailSender mailSender, 
            OrderGrouper orderGrouper, InlineTemplateService inlineTemplateService, AttachmentService attachmentService) {
        this.orderDAO = orderDAO;
        this.mailSender = mailSender;
        this.configurationService = configurationService;
        this.orderGrouper = orderGrouper;
        this.inlineTemplateService = inlineTemplateService;
        this.attachmentService = attachmentService;
        this.templateConfiguration = getTemplateConfiguration();
        this.templateConfiguration.setClassForTemplateLoading(getClass(), "/products/emails/");
    }

    protected Configuration getTemplateConfiguration() {
        return new Configuration();
    }

    @Transactional
    public void send(String orderId) throws ServiceException {
        Order order = orderDAO.findOne(orderId);
        if (isNotBlank(order.getNotified())) {
            LOG.info("Notify for already notified order: {}", orderId);
            return;
        }
        
        LOG.info("Notifying order: {}", order.getId());
        
        Map<String, Order> productIdOrders = orderGrouper.byProductGroup(order);
        try {
            for (Map.Entry<String, Order> productIdOrder : productIdOrders.entrySet()) {
                notifyOrderWithProductId(productIdOrder.getKey(), productIdOrder.getValue());
            }
        } catch (TemplateException | IOException | MessagingException | InterruptedException e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException(e);
        }
        
        setNotifed(order);
        LOG.info("Notified order: {}", order.getId());
    }

    @Transactional
    private void setNotifed(Order order) {
        order.setNotified(new LocalDateTime().toString());
        orderDAO.save(order);
    }
    
    private void notifyOrderWithProductId(String productId, Order order) throws TemplateException, IOException, MessagingException, InterruptedException {
        Item first = order.getItems().get(0);
        if (isNotBlank(first.getNotifyBusinessEmail())) {
            notifyBusinessOrder(productId, order, first.getNotifyBusinessEmail(), first.getNotifyBusinessEmailSubject(), first.getNotifyBusinessFormFilename());
        }
        
        String customerEmailField = first.getNotifyCustomerEmailField();
        if (isNotBlank(customerEmailField)) {
            String customerEmailTo = getCustomerEmailTo(order, customerEmailField);
            notifyCustomerOrder(productId, order, customerEmailTo, first.getNotifyCustomerEmailSubject(), first.getNotifyCustomerFormFilename());
        }
    }

    private String getCustomerEmailTo(Order order, String customerEmailField) {
        return "customerDetails".equals(customerEmailField) ? 
                order.getCustomerDetailsMap().get("email") : order.getDeliveryDetailsMap().get("email");
    }
    
    private void notifyBusinessOrder(String productId, Order order, String to, String subject, String filename) 
        throws TemplateException, IOException, MessagingException, InterruptedException {
        if (isBlank(to)) {
            LOG.info("No business email to notify for order receipt: {}", order.getReceipt());
            return;
        }
        
        Map<String, byte[]> attachments = attachmentService.retrieve(order, NotifyType.BUSINESS);
        
        String emailBody = prepareTemplate(productId, order, "business");
        LOG.info("Sending business email to: {} for order receipt: {}", to, order.getReceipt());
        sendEmail(order, to, subject, emailBody, filename, attachments);
    }

    private void notifyCustomerOrder(String productId, Order order, String to, String subject, String filename) throws TemplateException, IOException, MessagingException, InterruptedException {
        if (isBlank(to)) {
            LOG.info("No customer email to notify for order receipt: {}", order.getReceipt());
            return;
        }
        
        Map<String, byte[]> attachments = attachmentService.retrieve(order, NotifyType.CUSTOMER);
        
        String emailBody = prepareTemplate(productId, order, "customer");
        LOG.info("Sending customer email to: {} for order receipt: {}", to, order.getReceipt());
        sendEmail(order, to, subject, emailBody, filename, attachments);
    }
    
    private void sendEmail(Order order, String to, String subject, String emailBody, String filename, Map<String, byte[]> attachments) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, !attachments.isEmpty());
    
        helper.setTo(to);
        helper.setSubject(inlineTemplateService.template("subject", subject, order));
        helper.setText(emailBody);
        helper.setFrom(configurationService.getMailFrom());
        
        int attachmentCounter = 0;
        for (Map.Entry<String, byte[]> attachment : attachments.entrySet()) {
            attachmentCounter++;
            helper.addAttachment(attachmentCounter + "-" + filename, new ByteArrayResource(attachment.getValue()));
        }
        
        mailSender.send(message);
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
