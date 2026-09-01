package au.gov.qld.pub.orders.dao;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import au.gov.qld.pub.orders.entity.NoticeToPay;

@Repository
public interface NoticeToPayDAO extends CrudRepository<NoticeToPay, String> {

    @Query("select case when count(n) > 0 then true else false end from NoticeToPay n where n.paymentInformationId=:paymentInformationId and n.notifiedAt >= :notifiedAt")
    boolean existsByPaymentInformationIdAndNotifiedAtAfter(@Param("paymentInformationId") String paymentInformationId, @Param("notifiedAt") LocalDateTime notifiedAt);

}
