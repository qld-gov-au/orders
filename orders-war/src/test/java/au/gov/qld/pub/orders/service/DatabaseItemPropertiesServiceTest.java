package au.gov.qld.pub.orders.service;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.event.ContextRefreshedEvent;

import au.gov.qld.pub.orders.dao.FileItemPropertiesDAO;
import au.gov.qld.pub.orders.dao.ItemPropertiesDAO;
import au.gov.qld.pub.orders.entity.ItemProperties;

import com.google.common.collect.ImmutableMap;

@RunWith(MockitoJUnitRunner.class)
public class DatabaseItemPropertiesServiceTest {
    private static final String PRODUCT_ID_1 = "test";
    private static final String PRODUCT_ID_2 = "test2";
    
    DatabaseItemPropertiesService service;
    
    @Mock ItemPropertiesDAO dao;
    @Mock ContextRefreshedEvent event;
    @Mock FileItemPropertiesDAO fileDao;
    @Mock ItemProperties itemProperties;

    Properties properties1;
    Properties properties2;
    
    @Before
    public void setUp() throws IOException {
        properties1 = new Properties();
        properties1.setProperty("productId", PRODUCT_ID_1);
        
        properties2 = new Properties();
        properties2.setProperty("productId", PRODUCT_ID_2);
        
        when(fileDao.findProductProperties()).thenReturn(ImmutableMap.of(PRODUCT_ID_1, properties1, PRODUCT_ID_2, properties2));
        when(dao.findOne(PRODUCT_ID_1)).thenReturn(null);
        when(dao.findOne(PRODUCT_ID_2)).thenReturn(null);
        
        service = new DatabaseItemPropertiesService(dao, fileDao);
    }

    @Test
    public void loadFileProductsWhenDoesNotExist() throws IOException {
        service.onApplicationEvent(null);
        verify(dao).save((ItemProperties)argThat(hasProperty("productId", is(PRODUCT_ID_1))));
        verify(dao).save((ItemProperties)argThat(hasProperty("productId", is(PRODUCT_ID_2))));
    }
    
    @Test
    public void doNotLoadFileProductWhenExists() throws IOException {
        when(dao.exists(PRODUCT_ID_1)).thenReturn(true);
        when(dao.exists(PRODUCT_ID_2)).thenReturn(true);
        service.onApplicationEvent(null);
        verify(dao, never()).save(argThat(isA(ItemProperties.class)));
    }
}
