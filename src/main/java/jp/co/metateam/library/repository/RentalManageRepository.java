package jp.co.metateam.library.repository;

import java.util.Date;
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

    @Query // 登録の在庫の貸出待ち、貸出中をDBから持ってきて、本の数を取得
    ("SELECT COUNT(rm) FROM RentalManage rm " +
            " WHERE rm.stock.id = ?1 " +
            " AND rm.status IN (0, 1) ")
    long countByStockIdAndStatus(String stockId);

    @Query // 登録の在庫の貸出待ち、貸出中をDBから持ってきて、期間が被っていない本の数を取得
    ("SELECT COUNT(rm) FROM RentalManage rm " +
            " WHERE rm.stock.id = ?1 " +
            " AND rm.status IN (0, 1) " +
            " AND rm.expectedReturnOn < ?2 " +
            " AND rm.expectedRentalOn > ?3")
    long countByStockIdAndStatusAndExpectedDates(String stockId, Date expectedReturnOn, Date expectedRentalOn);

    @Query // 変更する在庫の貸出待ち、貸出中をDBから持ってきて、本の数を取得
    ("SELECT COUNT(rm) FROM RentalManage rm " +
            " WHERE rm.stock.id = ?1 " +
            " AND rm.status IN (0, 1) " +
            " AND rm.id != ?2 ")
    long countByStockIdAndStatusIn(String stockId, Long ID);

    @Query // 変更する在庫の貸出待ち、貸出中をDBから持ってきて、期間が被っていない本の数を取得
    ("SELECT COUNT(rm) FROM RentalManage rm " +
            " WHERE rm.stock.id = ?1 " +
            " AND rm.status IN (0, 1) " +
            " AND rm.id != ?2 " +
            " AND rm.expectedReturnOn < ?3 " +
            " OR rm.expectedRentalOn > ?4")
    long countByStockIdAndStatusInAndExpectedDates(String stockId, Long ID, Date expectedRentalOn,
            Date expectedReturnOn);
}