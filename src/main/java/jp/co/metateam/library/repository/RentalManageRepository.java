package jp.co.metateam.library.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.metateam.library.model.RentalManage;

@Repository
public interface RentalManageRepository extends JpaRepository<RentalManage, Long> {
    List<RentalManage> findAll();

	Optional<RentalManage> findById(Long id);

    //貸出登録可否チェック用のリスト
    @Query
    ("SELECT rm FROM RentalManage rm " +
    " WHERE ( rm.status=0 OR rm.status=1 ) " +
    " AND ?1 = rm.stock.id " )
    List<RentalManage> findByStockIdAndStatusIn(String StockId);

    //貸出編集可否チェック用のリスト取得
    @Query
     ("SELECT rm FROM RentalManage rm " +
      " WHERE ( rm.status=0 OR rm.status=1 ) " +
      " AND ?1 = rm.stock.id " +
      " AND ?2 <> rm.id ")
        List<RentalManage> findByStockIdAndStatusIn(String StockId, Long rentalId);
}
