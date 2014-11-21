package au.gov.qld.bdm.orders.dao;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import au.gov.qld.bdm.orders.entity.Order;

@Repository
public interface OrderDAO extends CrudRepository<Order, String> {
	Order findByCartId(String cartId);
}
