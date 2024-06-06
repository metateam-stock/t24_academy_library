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

    // タイトル、在庫数
    @Query("SELECT s.bookMst.title, COUNT(s) FROM Stock s JOIN s.bookMst WHERE s.status = 0 GROUP BY s.bookMst.title")
    List<Object[]> countByTotalStockPerTitle();

}
