package jp.co.metateam.library.service;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

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

@Service
public class RentalManageService {
    
    private final AccountRepository accountRepository;
    private final RentalManageRepository rentalManageRepository;
    private final StockRepository stockRepository;

     @Autowired
    public RentalManageService(
        AccountRepository accountRepository,
        RentalManageRepository rentalManageRepository,
        StockRepository stockRepository
    ) {
        this.accountRepository = accountRepository;
        this.rentalManageRepository = rentalManageRepository;
        this.stockRepository = stockRepository;
    }

    @Transactional
    public List <RentalManage> findAll() {
        List <RentalManage> rentalManageList = this.rentalManageRepository.findAll();

        return rentalManageList;
    }

    @Transactional
    public RentalManage findById(Long id) {
        return this.rentalManageRepository.findById(id).orElse(null);
    }

    //貸出登録の貸出可否チェック用のリスト取得
    public List<RentalManage> findByStockIdAndStatusIn(String StockId){
        List<RentalManage> rentalAvailable =this.rentalManageRepository.findByStockIdAndStatusIn(StockId);
        return rentalAvailable;
    }
    
    //貸出編集の貸出可否チェック用のリスト取得
    public List<RentalManage> findByStockIdAndStatusIn(String StockId, Long retalId){
        List<RentalManage> rentalAvailable =this.rentalManageRepository.findByStockIdAndStatusIn(StockId,retalId);
        return rentalAvailable;
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
            // 既存レコード取得
            Account account = this.accountRepository.findByEmployeeId(rentalManageDto.getEmployeeId()).orElse(null);
            RentalManage updateTargetBook = this.rentalManageRepository.findById(id).orElse(null);
            Stock stock = this.stockRepository.findById(rentalManageDto.getStockId()).orElse(null);

        
            if (updateTargetBook == null) {
                throw new Exception("RentalManage record not found.");
            }
            if (account == null) {
                throw new Exception("Account not found.");
            }
            if (stock == null) {
                throw new Exception("Stock not found.");
            }

            //updateTargetBook.setId(rentalManageDto.getId());
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
        
        if (status == RentalStatus.RENTAlING.getValue()) {
            rentalManage.setRentaledAt(timestamp);
        } else if (status == RentalStatus.RETURNED.getValue()) {
            rentalManage.setReturnedAt(timestamp);
        } else if (status == RentalStatus.CANCELED.getValue()) {
            rentalManage.setCanceledAt(timestamp);
        }

        return rentalManage;
    }

   
}

    


