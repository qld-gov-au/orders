package au.gov.qld.bdm.orders.dao;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import au.gov.qld.bdm.orders.entity.Item;

@Repository
public interface ItemDAO extends CrudRepository<Item, String> {
}
