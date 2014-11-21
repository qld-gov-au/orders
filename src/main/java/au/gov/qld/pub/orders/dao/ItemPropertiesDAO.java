package au.gov.qld.pub.orders.dao;

import java.util.Properties;

import org.springframework.stereotype.Repository;

@Repository
public interface ItemPropertiesDAO {

	Properties find(String productId);

}
