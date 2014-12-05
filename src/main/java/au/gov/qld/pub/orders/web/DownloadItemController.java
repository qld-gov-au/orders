package au.gov.qld.pub.orders.web;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import au.gov.qld.pub.orders.dao.ItemDAO;
import au.gov.qld.pub.orders.dao.OrderDAO;
import au.gov.qld.pub.orders.entity.Item;
import au.gov.qld.pub.orders.service.AttachmentService;
import au.gov.qld.pub.orders.service.NotifyService;
import au.gov.qld.pub.orders.service.NotifyType;
import au.gov.qld.pub.orders.service.OrderService;
import au.gov.qld.pub.orders.service.ServiceException;

@Controller
public class DownloadItemController {
    private static final Logger LOG = LoggerFactory.getLogger(DownloadItemController.class);
	private static final String CONTENT_TYPE = "application/pdf";
	
	private final OrderService orderService;
	private final NotifyService notifyService;
	private final ItemDAO itemDao;
	private final AttachmentService attachmentService;
	private final OrderDAO orderDao;
    
    @Autowired
    public DownloadItemController(OrderService orderService, NotifyService notifyService, AttachmentService attachmentService, ItemDAO itemDao, OrderDAO orderDao) {
        this.orderService = orderService;
        this.notifyService = notifyService;
		this.itemDao = itemDao;
		this.attachmentService = attachmentService;
		this.orderDao = orderDao;
    }
    
    @RequestMapping(value = "/download/{orderId}/{itemId}")
    public void download(@PathVariable String orderId, @PathVariable String itemId, HttpServletResponse response) throws IOException, ServiceException {
        LOG.info("Downloading item: {}", itemId);
        
        Item item = itemDao.findOne(itemId);
        if (item == null) {
        	throw new IllegalArgumentException("Unknown item id: " + itemId);
        }
        
        if (!item.isPaid()) {
        	orderService.notifyPayment(orderId);
        	notifyService.send(orderId);
        	item = itemDao.findOne(itemId);
        }
        
        if (!item.isPaid()) {
        	throw new IllegalArgumentException("Attempted to download unpaid item id: " + itemId + " with order id" + orderId);
        }

        String filename = item.getNotifyCustomerFormFilename();
        byte[] data = attachmentService.retrieve(orderDao.findOne(orderId), NotifyType.CUSTOMER).get(itemId);
        
        response.setContentType(CONTENT_TYPE);
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
		IOUtils.write(data, response.getOutputStream());
        response.getOutputStream().flush();   
        response.getOutputStream().close();
    }
}
