package au.gov.qld.pub.orders.web;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.nio.charset.Charset;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletResponse;

import com.google.common.collect.ImmutableMap;

import au.gov.qld.pub.orders.dao.ItemDAO;
import au.gov.qld.pub.orders.dao.OrderDAO;
import au.gov.qld.pub.orders.entity.Item;
import au.gov.qld.pub.orders.entity.Order;
import au.gov.qld.pub.orders.service.AttachmentService;
import au.gov.qld.pub.orders.service.FileAttachment;
import au.gov.qld.pub.orders.service.NotifyService;
import au.gov.qld.pub.orders.service.NotifyType;
import au.gov.qld.pub.orders.service.OrderGrouper;
import au.gov.qld.pub.orders.service.OrderService;

@RunWith(MockitoJUnitRunner.class)
public class DownloadItemControllerTest {
    
    private static final String PRODUCT_GROUP = "product group";
	private static final String ITEM_ID = "some item id";
    private static final String ORDER_ID = "some order id";
    private static final String FILENAME = "some filename";

    DownloadItemController controller;
    
    @Mock OrderService orderService;
    @Mock NotifyService notifyService;
    @Mock AttachmentService attachmentService;
    @Mock ItemDAO itemDao;
    @Mock OrderDAO orderDao;
    MockHttpServletResponse response;
    @Mock Item unpaidItem;
    @Mock Item paidItem;
    @Mock Order order;
    @Mock Order groupedOrder;
    @Mock OrderGrouper orderGrouper;
    @Mock FileAttachment attachment;
    
    @Before
    public void setUp() throws Exception {
    	when(attachment.getData()).thenReturn("test".getBytes(Charset.defaultCharset()));
    	response = new MockHttpServletResponse();
        when(unpaidItem.getId()).thenReturn(ITEM_ID);
        when(paidItem.getId()).thenReturn(ITEM_ID);
        when(paidItem.getProductGroup()).thenReturn(PRODUCT_GROUP);
        when(groupedOrder.getId()).thenReturn(ORDER_ID);
        
        when(paidItem.getNotifyCustomerFormFilename()).thenReturn(FILENAME);
        when(unpaidItem.isPaid()).thenReturn(false);
        when(paidItem.isPaid()).thenReturn(true);
        when(itemDao.findById(ITEM_ID)).thenReturn(Optional.of(unpaidItem), Optional.of(paidItem));
        when(orderDao.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(orderGrouper.paidByProductGroup(order)).thenReturn(ImmutableMap.of(PRODUCT_GROUP, groupedOrder));
        
        controller = new DownloadItemController(orderService, attachmentService, itemDao, orderDao, orderGrouper);
    }

    @Test
    public void outputAttachmentForUnpaidItemToResponseAfterCheckingPaid() throws Exception {
        when(attachmentService.retrieve(groupedOrder, NotifyType.CUSTOMER, ITEM_ID)).thenReturn(attachment);
        controller.download(ORDER_ID, ITEM_ID, response);
        
        verify(orderService).notifyPayment(ORDER_ID);
        assertThat(response.getContentType(), is("application/pdf"));
        assertThat(response.getHeader("Content-Disposition"), is("attachment; filename=\"" + FILENAME + "\""));
        assertThat(response.getContentAsString(), is("test"));
    }
    
    @Test
    public void immediatelyOutputAttachmentForItemWhenAlreadyPaid() throws Exception {
        when(attachmentService.retrieve(groupedOrder, NotifyType.CUSTOMER, ITEM_ID)).thenReturn(attachment);
        when(itemDao.findById(ITEM_ID)).thenReturn(Optional.of(paidItem));
        controller.download(ORDER_ID, ITEM_ID, response);
        
        verifyZeroInteractions(orderService);
        verifyZeroInteractions(notifyService);

        assertThat(response.getContentType(), is("application/pdf"));
        assertThat(response.getHeader("Content-Disposition"), is("attachment; filename=\"" + FILENAME + "\""));
        assertThat(response.getContentAsString(), is("test"));
    }
    
    @Test
    public void throwExceptionWhenItemNotPaid() throws Exception {
        when(itemDao.findById(ITEM_ID)).thenReturn(Optional.of(unpaidItem), Optional.of(unpaidItem));
        try {
            controller.download(ORDER_ID, ITEM_ID, response);
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("Attempted to download unpaid item id: " + ITEM_ID + " with order id" + ORDER_ID));
        } finally {
            verify(orderService).notifyPayment(ORDER_ID);
            verifyZeroInteractions(attachmentService);
        }
    }
    
    @Test
    public void throwExceptionWhenItemNotExists() throws Exception {
        when(itemDao.findById(ITEM_ID)).thenReturn(Optional.empty());
        try {
            controller.download(ORDER_ID, ITEM_ID, response);
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString(ITEM_ID));
        } finally {
            verifyZeroInteractions(notifyService);
            verifyZeroInteractions(orderService);
            verifyZeroInteractions(attachmentService);
        }
    }
}
