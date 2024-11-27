package au.gov.qld.pub.orders.web;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import au.gov.qld.pub.orders.service.NotifyService;
import au.gov.qld.pub.orders.service.OrderService;
import au.gov.qld.pub.orders.service.ServiceException;

@ExtendWith(MockitoExtension.class)
public class NotifyControllerTest {
    @Mock OrderService orderService;
    @Mock NotifyService notifyService;

    NotifyController controller;

    @BeforeEach
    public void setUp() {
        controller = new NotifyController(orderService);
    }

    @Test
    public void okNotifyOk() throws ServiceException, InterruptedException {
        ResponseEntity<String> entity = controller.confirm("id");
        assertThat(entity.getStatusCode(), is(HttpStatus.OK));
        verify(orderService).notifyPayment("id");
    }

    @Test
    public void notFoundWhenException() throws ServiceException {
        doThrow(new RuntimeException()).when(orderService).notifyPayment(anyString());
        ResponseEntity<String> entity = controller.confirm("unknown");
        assertThat(entity.getStatusCode(), is(HttpStatus.NOT_FOUND));
        verify(orderService).notifyPayment("unknown");
        verifyNoInteractions(notifyService);
    }
}
