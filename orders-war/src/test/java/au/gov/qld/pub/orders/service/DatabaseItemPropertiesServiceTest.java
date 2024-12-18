package au.gov.qld.pub.orders.service;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.event.ContextRefreshedEvent;

import au.gov.qld.pub.orders.dao.FileItemPropertiesDAO;
import au.gov.qld.pub.orders.dao.ItemPropertiesDAO;
import au.gov.qld.pub.orders.entity.ItemProperties;

import com.google.common.collect.ImmutableMap;

@ExtendWith(MockitoExtension.class)
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
    
    @BeforeEach
    public void setUp() throws IOException {
        properties1 = new Properties();
        properties1.setProperty("productId", PRODUCT_ID_1);
        
        properties2 = new Properties();
        properties2.setProperty("productId", PRODUCT_ID_2);
        
        when(fileDao.findProductProperties()).thenReturn(ImmutableMap.of(PRODUCT_ID_1, properties1, PRODUCT_ID_2, properties2));
//        when(dao.findById(PRODUCT_ID_1)).thenReturn(Optional.empty()); //UnnecessaryStubbingException
//        when(dao.findById(PRODUCT_ID_2)).thenReturn(Optional.empty()); //UnnecessaryStubbingException
        
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
        when(dao.existsById(PRODUCT_ID_1)).thenReturn(true);
        when(dao.existsById(PRODUCT_ID_2)).thenReturn(true);
        service.onApplicationEvent(null);
        verify(dao, never()).save(argThat(isA(ItemProperties.class)));
    }
}
