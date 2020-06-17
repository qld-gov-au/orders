package au.gov.qld.pub.orders.dao;

import java.util.Collection;
import java.util.Date;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import au.gov.qld.pub.orders.entity.FormFile;

@Repository
public interface FormFileDAO extends CrudRepository<FormFile, String> {

	@Query("select id from FormFile where createdAt <= :createdAt")
	Collection<String> findIdByCreatedAtBefore(Date createdAt);

}