package jp.co.metateam.library.repository;

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

@Query //ステータスが０か１の時の今回登録する在庫管理番号を取得
(" SELECT rm FROM RentalManage rm "+
//rentalManageの情報を全部持ってくる
" WHERE (rm.status =  0 OR rm.status = 1) "+
//ステータスが０か１の時
 " AND rm.stock.id = ?1 ")
 
 //rentalManageに入っている全在庫管理番号　= 今回登録する在庫管理番号に絞る　
 //？1とString 第一引数をつなげる
 List<RentalManage> findByStockIdAndStatusIn(String stockId);
 @Query
 (" SELECT rm FROM RentalManage rm "+
 " WHERE (rm.status =  0 OR rm.status = 1) "+
  " AND rm.stock.id = ?1 "+
  " AND id <> ?2 ")
 List<RentalManage> findByStockIdAndStatusIn(String stockId,Long rentalId);

}
 
