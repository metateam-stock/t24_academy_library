package jp.co.metateam.library.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jp.co.metateam.library.model.Stock;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    List<Stock> findAll();

    List<Stock> findByDeletedAtIsNull();

    List<Stock> findByDeletedAtIsNullAndStatus(Integer status);

    Optional<Stock> findById(String id);

    List<Stock> findByBookMstIdAndStatus(Long book_id, Integer status);

    // @Query("SELECT id FROM Stock WHERE book_id = AND status = 0")
    // List<String> findAllTitles();

    // SELECT COUNT Stock WHERE book_id = bookId AND status = 0

    // 書籍マスタテーブルと在庫テーブルを結合して、「書籍名」と「在庫管理番号の数」を取得
    @Query(value = "SELECT b.title, COUNT(s.id) AS stockCount " +
            "FROM book_mst b " +
            "INNER JOIN stocks s ON b.id = s.book_id " +
            "WHERE s.status = 0 " +
            "GROUP BY b.title", nativeQuery = true)
    List<Object[]> findTitleAndCountId();
}
