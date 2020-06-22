package au.gov.qld.pub.orders.dao;

import java.util.Date;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import au.gov.qld.pub.orders.entity.Order;

@Repository
public interface OrderDAO extends CrudRepository<Order, String> {
    Order findByCartId(String cartId);
    
    @Query("select o.id from Order o where o.created >= :created and o.paid is null")
    Iterable<String> findUnpaidOrdersCreatedAfter(@Param("created") Date created);

    @Query("from Order o where o.created <= :created and (o.paid <> '' and o.paid is not null)")
    Iterable<Order> findOlderThanCreatedAndPaid(@Param("created") Date created);
    
    @Query("from Order o where o.created <= :created and (o.paid = '' or o.paid is null)")
    Iterable<Order> findOlderThanCreatedAndNotPaid(@Param("created") Date created);
}
