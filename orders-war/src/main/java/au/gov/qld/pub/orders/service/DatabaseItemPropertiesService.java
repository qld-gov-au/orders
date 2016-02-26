package au.gov.qld.pub.orders.service;

import org.springframework.beans.factory.annotation.Autowired;

import au.gov.qld.pub.orders.dao.ItemPropertiesDAO;
import au.gov.qld.pub.orders.entity.ItemProperties;

public class DatabaseItemPropertiesService implements ItemPropertiesService {

    private final ItemPropertiesDAO dao;

    @Autowired
    public DatabaseItemPropertiesService(ItemPropertiesDAO dao) {
        this.dao = dao;
    }
    
    @Override
    public ItemProperties find(String productId) {
        return dao.findOne(productId);
    }

}
