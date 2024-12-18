package au.gov.qld.pub.orders.web;

import java.io.IOException;
import java.util.Map;

import jakarta.servlet.http.HttpServletResponse;

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
import au.gov.qld.pub.orders.entity.Order;
import au.gov.qld.pub.orders.service.AttachmentService;
import au.gov.qld.pub.orders.service.FileAttachment;
import au.gov.qld.pub.orders.service.NotifyType;
import au.gov.qld.pub.orders.service.OrderGrouper;
import au.gov.qld.pub.orders.service.OrderService;
import au.gov.qld.pub.orders.service.ServiceException;

@Controller
public class DownloadItemController {
    private static final String ATTACHMENT_HEADER = "Content-Disposition";
	private static final String ATTACHMENT_FILENAME_FMT = "attachment; filename=\"%s\"";
	private static final Logger LOG = LoggerFactory.getLogger(DownloadItemController.class);
    private static final String CONTENT_TYPE = "application/pdf";

    private final OrderService orderService;
    private final ItemDAO itemDao;
    private final AttachmentService attachmentService;
    private final OrderDAO orderDao;
	private final OrderGrouper orderGrouper;

    @Autowired
    public DownloadItemController(OrderService orderService, AttachmentService attachmentService, ItemDAO itemDao, OrderDAO orderDao, OrderGrouper orderGrouper) {
        this.orderService = orderService;
        this.itemDao = itemDao;
        this.attachmentService = attachmentService;
        this.orderDao = orderDao;
		this.orderGrouper = orderGrouper;
    }

    @RequestMapping(value = "/download/{orderId}/{itemId}")
    public void download(@PathVariable("orderId") String orderId, @PathVariable("itemId") String itemId, HttpServletResponse response) throws IOException, ServiceException, InterruptedException {
        LOG.info("Downloading item: {}", itemId);

        Item item = itemDao.findById(itemId).orElse(null);
        if (item == null) {
            throw new IllegalArgumentException("Unknown item id: " + itemId);
        }

        if (!item.isPaid()) {
            orderService.notifyPayment(orderId);
            item = itemDao.findById(itemId).orElse(null);
            if (!item.isPaid()) {
                throw new IllegalArgumentException("Attempted to download unpaid item id: " + itemId + " with order id" + orderId);
            }
        }

        Order order = orderDao.findById(orderId).orElse(null);
		Map<String, Order> groupedOrders = orderGrouper.paidByProductGroup(order);
		FileAttachment attachment = attachmentService.retrieve(groupedOrders.get(item.getProductGroup()), NotifyType.CUSTOMER, itemId);

        response.setContentType(CONTENT_TYPE);
        response.setHeader(ATTACHMENT_HEADER, String.format(ATTACHMENT_FILENAME_FMT, item.getNotifyCustomerFormFilename()));
        IOUtils.write(attachment.getData(), response.getOutputStream());
        response.getOutputStream().flush();
        response.getOutputStream().close();
    }
}
