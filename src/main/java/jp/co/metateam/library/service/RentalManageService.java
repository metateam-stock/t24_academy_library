package jp.co.metateam.library.service;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import io.micrometer.common.util.StringUtils;
import jp.co.metateam.library.model.Account;
import jp.co.metateam.library.model.BookMst;
import jp.co.metateam.library.model.BookMstDto;
import jp.co.metateam.library.model.RentalManage;
import jp.co.metateam.library.model.RentalManageDto;
import jp.co.metateam.library.model.Stock;
import jp.co.metateam.library.model.StockDto;
import jp.co.metateam.library.repository.AccountRepository;
import jp.co.metateam.library.repository.BookMstRepository;
import jp.co.metateam.library.repository.RentalManageRepository;
import jp.co.metateam.library.repository.StockRepository;
import jp.co.metateam.library.values.RentalStatus;
import jp.co.metateam.library.values.StockStatus;

@Service
public class RentalManageService {

    private final AccountRepository accountRepository;
    private final RentalManageRepository rentalManageRepository;
    private final StockRepository stockRepository;
    private final StockService stockService;

    @Autowired
    public RentalManageService(
            AccountRepository accountRepository,
            RentalManageRepository rentalManageRepository,
            StockRepository stockRepository,
            StockService stockService) {
        this.accountRepository = accountRepository;
        this.rentalManageRepository = rentalManageRepository;
        this.stockRepository = stockRepository;
        this.stockService = stockService;
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

    // 貸出登録の貸出可否チェック用のリスト取得
    public List<RentalManage> findByStockIdAndStatusIn(String StockId) {
        List<RentalManage> rentalAvailable = this.rentalManageRepository
            .findByStockIdAndStatusIn(StockId);
        return rentalAvailable;
    }

    // 貸出編集の貸出可否チェック用のリスト取得
    public List<RentalManage> findByStockIdAndStatusIn(String StockId, Long retalId) {
        List<RentalManage> rentalAvailable = this.rentalManageRepository
            .findByStockIdAndStatusIn(StockId, retalId);
        return rentalAvailable;
    }

    @Transactional
    public void save(RentalManageDto rentalManageDto, BindingResult result, String stockId) throws Exception {
        try {
            Stock rentalBook = stockService.findById(stockId);
            Account account = this.accountRepository.findByEmployeeId(rentalManageDto.getEmployeeId()).orElse(null);
            
            if (account == null) {
                result.addError(new FieldError("rentalManageDto", "employeeId", "選択された社員番号は存在しません"));
            }

            if (rentalBook.getStatus() == StockStatus.RENT_NOT_AVAILABLE.getValue()) {
                result.addError(new FieldError("rentalManageDto", "stockId", "選択された在庫管理番号が存在しません"));
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
    public void update(Long rentalId, RentalManageDto rentalManageDto, BindingResult result, String stockId)
            throws Exception {
        try {
            //RentalManage rentalManage = this.findById(rentalId);
            Account account = this.accountRepository.findByEmployeeId(rentalManageDto.getEmployeeId()).orElse(null);
            RentalManage updateTargetBook = this.rentalManageRepository.findById(rentalId).orElse(null);
            Stock stock = this.stockRepository.findById(rentalManageDto.getStockId()).orElse(null);
            Stock rentalBook = stockService.findById(stockId);

            // コントローラーで表示した感じで
            if (updateTargetBook == null) {
                throw new Exception("RentalManage record not found.");
            }
            if (account == null) {
                result.addError(new FieldError("rentalManageDto", "employeeId", "選択された社員番号は存在しません"));
            }
            if (stock == null) {
                throw new Exception("Stock not found.");
            }

            if (rentalBook.getStatus() == StockStatus.RENT_NOT_AVAILABLE.getValue()) {
                result.addError(new FieldError("rentalManageDto", "stockId", "選択された在庫管理番号が存在しません"));
            }

            if (result.hasErrors()) {
                throw new Exception("Validation error.");
            }

            updateTargetBook = setRentalStatusDate(updateTargetBook, rentalManageDto.getStatus());
            
            updateTargetBook.setAccount(account);
            updateTargetBook.setExpectedRentalOn(rentalManageDto.getExpectedRentalOn());
            updateTargetBook.setExpectedReturnOn(rentalManageDto.getExpectedReturnOn());
            updateTargetBook.setStatus(rentalManageDto.getStatus());
            updateTargetBook.setStock(stock);

            // データベースへの保存
            this.rentalManageRepository.save(updateTargetBook);
        } catch (Exception e) {
            throw e;
        }
    }

    private RentalManage setRentalStatusDate(RentalManage rentalManage, Integer status) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        if (status == RentalStatus.RENTALING.getValue()) {
            rentalManage.setRentaledAt(timestamp);
        } else if (status == RentalStatus.RETURNED.getValue()) {
            rentalManage.setReturnedAt(timestamp);
        } else if (status == RentalStatus.CANCELED.getValue()) {
            rentalManage.setCanceledAt(timestamp);
        }

        return rentalManage;
    }

}