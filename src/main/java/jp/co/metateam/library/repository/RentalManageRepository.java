package jp.co.metateam.library.repository;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jp.co.metateam.library.model.RentalManage;

@Repository
public interface RentalManageRepository extends JpaRepository<RentalManage, Long> {
    List<RentalManage> findAll();

	Optional<RentalManage> findById(Long id);

@Query("SELECT COUNT(*) FROM Stock WHERE id = ?1 AND status = 0")
Integer count(String id);

@Query("SELECT COUNT(*) FROM RentalManage WHERE stock.id = ?1 AND status IN (0,1)")
Integer  addtest(String stockId );

@Query("SELECT COUNT(*) FROM RentalManage WHERE stock.id = ?1 AND status IN (0,1) AND (expectedRentalOn > ?2 OR expectedReturnOn < ?3)")
Integer addwhetherDay(String stockId, Date expected_return_on, Date expected_rental_on);

@Query("SELECT COUNT(*) FROM RentalManage WHERE stock.id = ?1 AND id != ?2 AND status IN (0,1)")
Integer  test(String stockId, Long id );

@Query("SELECT COUNT(*) FROM RentalManage WHERE stock.id = ?1 AND id != ?2 AND status IN (0,1) AND (expectedRentalOn > ?3 OR expectedReturnOn < ?4)")
Integer whetherDay(String stockId, Long id, Date expected_return_on, Date expected_rental_on);

}
