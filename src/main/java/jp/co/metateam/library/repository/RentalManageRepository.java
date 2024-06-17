package jp.co.metateam.library.repository;

import java.util.List;
import java.util.Date;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jp.co.metateam.library.model.BookMst;
import jp.co.metateam.library.model.RentalManage;

@Repository
public interface RentalManageRepository extends JpaRepository<RentalManage, Long> {
    List<RentalManage> findAll();

	Optional<RentalManage> findById(Long id);


    
    //追加箇所
    //「貸出待ち」と「貸出中」を全件取得
    @Query("SELECT COUNT(rm) FROM RentalManage rm WHERE rm.stock.id = ?1 AND rm.status IN (0, 1)")
    long countByStockIdAndStatusIn(String stockId);
    //自分のID以外の「貸出待ち」と「貸出中」を全件取得
    @Query("SELECT COUNT(rm) FROM RentalManage rm WHERE rm.stock.id = ?1 AND rm.status IN (0, 1) AND rm.id <> ?2")
    long countByStockIdAndStatusInAndIdNot(String stockId, Long id);
    //期間の重複チェック(重複していない個数の確認)
    @Query("SELECT COUNT(rm) FROM RentalManage rm WHERE rm.stock.id = ?1 AND rm.status IN (0, 1) AND (rm.expectedRentalOn > ?2 OR rm.expectedReturnOn < ?3)")
    long countByStockIdAndStatusAndTermsIn(String stockId, Date expectedReturnOn, Date expectedRentalOn);
    //自分以外の期間の重複チェック
    @Query("SELECT COUNT(rm) FROM RentalManage rm WHERE rm.stock.id = ?1 AND rm.status IN (0, 1) AND rm.id <> ?2 AND (rm.expectedRentalOn > ?3 OR rm.expectedReturnOn < ?4)")
    long countByStockIdAndStatusAndIdNotAndTermsIn(String stockId, Long id, Date expectedReturnOn, Date expectedRentalOn);
 
    @Query(value = "SELECT COUNT(*) FROM rental_manage rm INNER JOIN stocks s ON rm.stock_id = s.id INNER JOIN book_mst bm ON s.book_id = bm.id WHERE bm.title = ?1 AND rm.expected_rental_on <= ?2 AND rm.expected_return_on >= ?2", nativeQuery = true)
    Long countByborrowingbook(String title, Date specifiedDate);

    //？は値が変わる
}
