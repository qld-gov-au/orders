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

@RunWith(MockitoJUnitRunner.class)
public class StartUpTest {
    
    private static final String PRODUCT_ID = "test";
    
    StartUp startUp;
    @Mock ItemPropertiesDAO dao;
    @Mock ContextRefreshedEvent event;
    @Mock FileItemPropertiesDAO fileDao;
    @Mock ItemProperties itemProperties;

    Properties properties;
    
    @Before
    public void setUp() {
        properties = new Properties();
        properties.setProperty("productId", PRODUCT_ID);
        when(fileDao.find(PRODUCT_ID)).thenReturn(properties);
        when(dao.findOne(PRODUCT_ID)).thenReturn(null);
    }

    @Test
    public void loadFileProductsWhenDoesNotExist() throws IOException {
        startUp = new StartUp(dao, fileDao);
        startUp.onApplicationEvent(event);
        verify(dao).save((ItemProperties)argThat(hasProperty("productId", is(PRODUCT_ID))));
    }
    
    @Test
    public void doNotLoadFileProductWhenExists() throws IOException {
        when(dao.exists(PRODUCT_ID)).thenReturn(true);
        startUp = new StartUp(dao, fileDao);
        startUp.onApplicationEvent(event);
        verify(dao, never()).save(argThat(isA(ItemProperties.class)));
    }
}