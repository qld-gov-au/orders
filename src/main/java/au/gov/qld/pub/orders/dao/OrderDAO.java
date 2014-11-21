package au.gov.qld.pub.orders.dao;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import au.gov.qld.pub.orders.entity.Order;

@Repository
public interface OrderDAO extends CrudRepository<Order, String> {
    Order findByCartId(String cartId);
}
