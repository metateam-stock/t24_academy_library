package jp.co.metateam.library.service;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jp.co.metateam.library.model.Account;
import jp.co.metateam.library.model.BookMst;
import jp.co.metateam.library.model.RentalManage;
import jp.co.metateam.library.model.RentalManageDto;
import jp.co.metateam.library.model.Stock;
import jp.co.metateam.library.repository.AccountRepository;
import jp.co.metateam.library.repository.RentalManageRepository;
import jp.co.metateam.library.repository.StockRepository;
import jp.co.metateam.library.service.RentalManageService;
import jp.co.metateam.library.service.StockService;
import jp.co.metateam.library.values.RentalStatus;

@Service
public class RentalManageService {

    private final AccountRepository accountRepository;
    private final RentalManageRepository rentalManageRepository;
    private final StockRepository stockRepository;

    @Autowired
    public RentalManageService(
            AccountRepository accountRepository,
            RentalManageRepository rentalManageRepository,
            StockRepository stockRepository) {
        this.accountRepository = accountRepository;
        this.rentalManageRepository = rentalManageRepository;
        this.stockRepository = stockRepository;
    }

    @Transactional
    public List<RentalManage> findAll() {
        List<RentalManage> rentalManageList = this.rentalManageRepository.findAll();

        return rentalManageList;
    }

    @Transactional
    public RentalManage findById(Long id) {
        return this.rentalManageRepository.findById(id).orElse(null);
    }

    @Transactional
    public void save(RentalManageDto rentalManageDto) throws Exception {
        try {
            Account account = this.accountRepository.findByEmployeeId(rentalManageDto.getEmployeeId()).orElse(null);
            if (account == null) {
                throw new Exception("Account not found.");
            }

            Stock stock = this.stockRepository.findById(rentalManageDto.getStockId()).orElse(null);
            if (stock == null) {
                throw new Exception("Stock not found.");
            }

            RentalManage rentalManage = new RentalManage();
            rentalManage = setRentalStatusDate(rentalManage, rentalManageDto.getStatus());

            rentalManage.setAccount(account);
            rentalManage.setExpectedRentalOn(rentalManageDto.getExpectedRentalOn());
            rentalManage.setExpectedReturnOn(rentalManageDto.getExpectedReturnOn());
            rentalManage.setStatus(rentalManageDto.getStatus());
            rentalManage.setStock(stock);

            // データベースへの保存
            this.rentalManageRepository.save(rentalManage);
        } catch (Exception e) {
            throw e;
        }
    }

    @Transactional
    public void update(Long id, RentalManageDto rentalManageDto) throws Exception {
        try {
            Account account = this.accountRepository.findByEmployeeId(rentalManageDto.getEmployeeId()).orElse(null);
            if (account == null) {
                throw new Exception("Account not found.");
            }

            Stock stock = this.stockRepository.findById(rentalManageDto.getStockId()).orElse(null);
            if (stock == null) {
                throw new Exception("Stock not found.");
            }

            // RentalManage rentalManage = new RentalManage(); ←すでに上のsaveメソッドでインスタンス化済み
            RentalManage rentalManage = findById(id);
            rentalManage = setRentalStatusDate(rentalManage, rentalManageDto.getStatus());

            rentalManage.setAccount(account);
            rentalManage.setExpectedRentalOn(rentalManageDto.getExpectedRentalOn());
            rentalManage.setExpectedReturnOn(rentalManageDto.getExpectedReturnOn());
            rentalManage.setStatus(rentalManageDto.getStatus());
            rentalManage.setStock(stock);
            rentalManage.setId(rentalManageDto.getId());

            // データベースへの保存
            this.rentalManageRepository.save(rentalManage);
        } catch (Exception e) {
            throw e;
        }
    }

    private RentalManage setRentalStatusDate(RentalManage rentalManage, Integer status) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        if (status == RentalStatus.RENTAlING.getValue()) {
            rentalManage.setRentaledAt(timestamp);
        } else if (status == RentalStatus.RETURNED.getValue()) {
            rentalManage.setReturnedAt(timestamp);
        } else if (status == RentalStatus.CANCELED.getValue()) {
            rentalManage.setCanceledAt(timestamp);
        }

        return rentalManage;
    }

    // rental addで使うList(SQLで取得したレコード)
    @Transactional
    public List<RentalManage> findByStockIdAndStatusIn(String Id) {
        List<RentalManage> rentalManages = this.rentalManageRepository.findByStockAndStatusIn(Id);
        return rentalManages;
    }

    // rental editで使うList(SQLで取得したレコード)
    @Transactional
    public List<RentalManage> findByStockIdAndStatusIn(String Id, Long rentalId) {
        List<RentalManage> rentalManages = this.rentalManageRepository.findByStockIdAndStatusIn(Id, rentalId);
        return rentalManages;
    }

    // 在庫ステータスチェック
    public String checkInventoryStatus(Long id) {
        RentalManage rentalManage = findById(id);
        if (rentalManage.getStatus() == 0) {
            return null; // 利用可→エラーメッセージなし
        } else {
            return "この本はご利用できません"; // 利用不可→エラーメッセージを設定
        }
    }

    // 貸出期間重複チェック「貸出登録の時」⇒貸出管理番号が付与されていない状態(引数が貸出編集の時と異なるため、違うメソッドが必要)
    public String firstAvailabilityCheckForLending(RentalManageDto rentalManageDto, String id) {
        List<RentalManage> rentalManageList = findByStockIdAndStatusIn(id);
        if (rentalManageList != null) {
            for (RentalManage rentalManage : rentalManageList) {
                if (rentalManageDto.getExpectedReturnOn().after(rentalManage.getExpectedRentalOn()) &&
                        rentalManageDto.getExpectedRentalOn().before(rentalManage.getExpectedReturnOn())) {
                    return "貸出期間が重複しているため、この本を借りることができません。日付を変更してください。";
                }
            }
            return null;
        } else {
            return null;
        }
    }

    // 貸出期間重複チェック「貸出編集の時」⇒貸出管理番号が付与されている状態(引数が貸出登録の時と異なるため、違うメソッドが必要)
    public String secondAvailabilityCheckForLending(RentalManageDto rentalManageDto, String id, Long rentalId) {
        List<RentalManage> rentalManageList = findByStockIdAndStatusIn(id, rentalId);
        if (rentalManageList != null) {
            for (RentalManage rentalManage : rentalManageList) {
                if (rentalManageDto.getExpectedReturnOn().after(rentalManage.getExpectedRentalOn()) &&
                        rentalManageDto.getExpectedRentalOn().before(rentalManage.getExpectedReturnOn())) {
                    return "貸出期間が重複しているため、この本を借りることができません。日付を変更してください。";
                }
            }
            return null;
        } else {
            return null;
        }
    }
}
