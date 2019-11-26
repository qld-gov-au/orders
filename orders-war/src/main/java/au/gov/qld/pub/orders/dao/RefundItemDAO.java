package au.gov.qld.pub.orders.dao;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import au.gov.qld.pub.orders.entity.RefundItem;
import au.gov.qld.pub.orders.entity.RefundState;

@Repository
public interface RefundItemDAO extends CrudRepository<RefundItem, String> {

	List<RefundItem> findByRefundState(RefundState refundState);

	List<RefundItem> findByOrderLineIdAndRefundState(String orderLineId, RefundState refundState);
}
