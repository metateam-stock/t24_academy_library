package jp.co.metateam.library.repository;

import java.util.List;
import java.util.Optional;
import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.metateam.library.model.RentalManage;

@Repository
public interface RentalManageRepository extends JpaRepository<RentalManage, Long> {
        List<RentalManage> findAll();

        Optional<RentalManage> findById(Long id);

        // SQLでデータを取得「貸出登録」
        @Query("select rm"
                        + " from RentalManage rm" + " where (rm.status = 0 or rm.status = 1)"
                        + " and rm.stock.id = ?1 ")
        List<RentalManage> findByStockAndStatusIn(String Id);

        // SQLでデータを取得「貸出編集」
        @Query("select rm"
                        + " from RentalManage rm" + " where (rm.status = 0 or rm.status = 1)"
                        + " and rm.stock.id = ?1 "
                        + " and rm.id <> ?2")
        List<RentalManage> findByStockIdAndStatusIn(String Id, Long rentalId);

        // 貸出予定日と返却予定日の間の指定された書籍の貸出数(COUNT)
        @Query(value = "SELECT COUNT(*) AS rentalCount "
                        + "FROM rental_manage rm "
                        + "JOIN stocks s ON rm.stock_id = s.id "
                        + "JOIN book_mst b ON s.book_id = b.id "
                        + "WHERE rm.expected_rental_on <= :date AND :date <= rm.expected_return_on "
                        + "AND b.title = :title", nativeQuery = true)
        Long findByTitleCount(@Param("title") String title,
                        @Param("date") Date date);

        // 貸出期間外の在庫管理番号(貸出登録画面に遷移した後にデータがセットされている状態にするためのSQL)
        @Query(value = "SELECT s.id "
                        + "FROM stocks s "
                        + "JOIN book_mst b ON s.book_id = b.id "
                        + "LEFT JOIN rental_manage rm ON s.id = rm.stock_id "
                        + "WHERE (rm.expected_rental_on > :date OR :date > rm.expected_return_on OR rm.stock_id IS NULL) "
                        + "AND s.status = 0 AND b.title = :title", nativeQuery = true)
        List<String> findByStockId(@Param("title") String title,
                        @Param("date") Date date);
}
