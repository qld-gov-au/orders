package au.gov.qld.pub.orders.web;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import au.gov.qld.pub.orders.dao.ItemDAO;
import au.gov.qld.pub.orders.dao.OrderDAO;
import au.gov.qld.pub.orders.entity.Item;
import au.gov.qld.pub.orders.entity.Order;
import au.gov.qld.pub.orders.service.AttachmentService;
import au.gov.qld.pub.orders.service.NotifyService;
import au.gov.qld.pub.orders.service.NotifyType;
import au.gov.qld.pub.orders.service.OrderService;

import com.google.common.collect.ImmutableMap;

@RunWith(MockitoJUnitRunner.class)
public class DownloadItemControllerTest {
    
    private static final String ITEM_ID = "some item id";
    private static final String ORDER_ID = "some order id";
    private static final String FILENAME = "some filename";

    DownloadItemController controller;
    
    @Mock OrderService orderService;
    @Mock NotifyService notifyService;
    @Mock AttachmentService attachmentService;
    @Mock ItemDAO itemDao;
    @Mock OrderDAO orderDao;
    @Mock HttpServletResponse response;
    @Mock ServletOutputStream output;
    @Mock Item unpaidItem;
    @Mock Item paidItem;
    @Mock Order order;
    
    @Before
    public void setUp() throws Exception {
        when(response.getOutputStream()).thenReturn(output);
        when(unpaidItem.getId()).thenReturn(ITEM_ID);
        when(paidItem.getId()).thenReturn(ITEM_ID);
        when(order.getId()).thenReturn(ORDER_ID);
        
        when(paidItem.getNotifyCustomerFormFilename()).thenReturn(FILENAME);
        when(unpaidItem.isPaid()).thenReturn(false);
        when(paidItem.isPaid()).thenReturn(true);
        when(itemDao.findOne(ITEM_ID)).thenReturn(unpaidItem, paidItem);
        when(orderDao.findOne(ORDER_ID)).thenReturn(order);
        
        controller = new DownloadItemController(orderService, attachmentService, itemDao, orderDao);
    }

    @Test
    public void outputAttachmentForUnpaidItemToResponseAfterCheckingPaid() throws Exception {
        when(attachmentService.retrieve(order, NotifyType.CUSTOMER)).thenReturn(ImmutableMap.of(ITEM_ID, "test".getBytes()));
        controller.download(ORDER_ID, ITEM_ID, response);
        
        verify(orderService).notifyPayment(ORDER_ID);
        verify(response).setContentType("application/pdf");
        verify(response).setHeader("Content-Disposition", "attachment; filename=\"" + FILENAME + "\"");
        verify(output).write("test".getBytes());
        verify(output).flush();
        verify(output).close();
    }
    
    @Test
    public void immediatelyOutputAttachmentForItemWhenAlreadyPaid() throws Exception {
        when(attachmentService.retrieve(order, NotifyType.CUSTOMER)).thenReturn(ImmutableMap.of(ITEM_ID, "test".getBytes()));
        when(itemDao.findOne(ITEM_ID)).thenReturn(paidItem);
        controller.download(ORDER_ID, ITEM_ID, response);
        
        verifyZeroInteractions(orderService);
        verifyZeroInteractions(notifyService);

        verify(response).setContentType("application/pdf");
        verify(response).setHeader("Content-Disposition", "attachment; filename=\"" + FILENAME + "\"");
        verify(output).write("test".getBytes());
        verify(output).flush();
        verify(output).close();
    }
    
    @Test
    public void throwExceptionWhenItemNotPaid() throws Exception {
        when(itemDao.findOne(ITEM_ID)).thenReturn(unpaidItem, unpaidItem);
        try {
            controller.download(ORDER_ID, ITEM_ID, response);
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("Attempted to download unpaid item id: " + ITEM_ID + " with order id" + ORDER_ID));
        } finally {
            verify(orderService).notifyPayment(ORDER_ID);
            verifyZeroInteractions(output);
            verifyZeroInteractions(attachmentService);
        }
    }
    
    @Test
    public void throwExceptionWhenItemNotExists() throws Exception {
        when(itemDao.findOne(ITEM_ID)).thenReturn(null);
        try {
            controller.download(ORDER_ID, ITEM_ID, response);
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString(ITEM_ID));
        } finally {
            verifyZeroInteractions(output);
            verifyZeroInteractions(notifyService);
            verifyZeroInteractions(orderService);
            verifyZeroInteractions(attachmentService);
        }
    }
}
