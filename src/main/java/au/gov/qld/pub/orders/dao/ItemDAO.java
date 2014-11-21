package au.gov.qld.pub.orders.dao;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import au.gov.qld.pub.orders.entity.Item;

@Repository
public interface ItemDAO extends CrudRepository<Item, String> {
}
