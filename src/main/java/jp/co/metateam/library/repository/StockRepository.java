package jp.co.metateam.library.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import jp.co.metateam.library.model.Stock;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    
    List<Stock> findAll();

    List<Stock> findByDeletedAtIsNull();

    List<Stock> findByDeletedAtIsNullAndStatus(Integer status);

	Optional<Stock> findById(String id);
    
    List<Stock> findByBookMstIdAndStatus(Long book_id,Integer status);

    @Query("SELECT bm.bookMst.title, COUNT(bm) FROM Stock bm JOIN bm.bookMst WHERE bm.status = 0 AND bm.deletedAt IS NULL GROUP BY bm.bookMst.title")
    List<Object[]> findByBookMstIdAndStatus();

    @Query("SELECT s.id FROM Stock s WHERE s.bookMst.title = :title")
    String findStockIdByTitle(@Param("title") String title);



}

