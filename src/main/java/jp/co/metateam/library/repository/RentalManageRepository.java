package jp.co.metateam.library.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import jp.co.metateam.library.model.RentalManage;

@Repository
public interface RentalManageRepository extends JpaRepository<RentalManage, Long> {
        List<RentalManage> findAll();

        Optional<RentalManage> findById(Long id);

        @Query // 登録の在庫の貸出待ち、貸出中をDBから持ってきて、本の数を取得
        ("SELECT COUNT(rm) FROM RentalManage rm WHERE rm.stock.id = ?1  AND rm.status IN (0, 1) ")
        long countByStockIdAndStatus(String stockId);

        @Query // 登録の在庫の貸出待ち、貸出中をDBから持ってきて、期間が被っていない本の数を取得
        ("SELECT COUNT(rm) FROM RentalManage rm WHERE rm.stock.id = ?1 AND rm.status IN (0, 1) AND rm.expectedReturnOn < ?2  AND rm.expectedRentalOn > ?3")
        long countByStockIdAndStatusAndExpectedDates(String stockId, Date expectedRentalOn, Date expectedReturnOn);

        @Query // 変更する在庫の貸出待ち、貸出中をDBから持ってきて、本の数を取得
        ("SELECT COUNT(rm) FROM RentalManage rm WHERE rm.stock.id = ?1 AND rm.status IN (0, 1)  AND rm.id != ?2 ")
        long countByStockIdAndStatusIn(String stockId, Long ID);

        @Query // 変更する在庫の貸出待ち、貸出中をDBから持ってきて、期間が被っていない本の数を取得
        ("SELECT COUNT(rm) FROM RentalManage rm WHERE rm.stock.id = ?1 AND rm.status IN (0, 1) AND rm.id != ?2 AND rm.expectedReturnOn < ?3  OR rm.expectedRentalOn > ?4")
        long countByStockIdAndStatusInAndExpectedDates(String stockId, Long ID, Date expectedRentalOn,
                        Date expectedReturnOn);

        @Query // タイトル別、日ごとの貸し出されている冊数
        (value = "SELECT COUNT(*) FROM rental_manage rm INNER JOIN stocks s ON rm.stock_id = s.id INNER JOIN book_mst bm ON s.book_id = bm.id "
                        +
                        " WHERE bm.title = ?1 AND (rm.expected_rental_on <= ?2 AND rm.expected_return_on >= ?2)", nativeQuery = true)
        Long countBySpecifiedDateRentals(String title, Date specifiedDate);

        // タイトル、在庫管理番号
        @Query(value = "SELECT s.id FROM stocks s Left JOIN rental_manage rm  ON s.id = rm.stock_id JOIN book_mst bm ON s.book_id = bm.id WHERE s.status = 0 AND bm.title= :title AND (rm.stock_id is null OR rm.expected_rental_on > :day OR rm.expected_return_on < :day OR rm.status = 3)", nativeQuery = true)
        List<String> selectByStockId(@Param("title") String title, @Param("day") Date specifiedDate);

}