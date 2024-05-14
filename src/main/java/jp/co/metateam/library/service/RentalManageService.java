package jp.co.metateam.library.service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

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

    @Transactional 
    public void update(Long id, RentalManageDto rentalManageDto) throws Exception {
        try {
            RentalManage rentalManage = this.rentalManageRepository.findById(id).orElse(null);
            if (rentalManage == null) {
                throw new Exception("RentalManage record not found.");
            }

            Account account = this.accountRepository.findByEmployeeId(rentalManageDto.getEmployeeId()).orElse(null);
            if (account == null) {
                throw new Exception("Account not found.");
            }

            Stock stock = this.stockRepository.findById(rentalManageDto.getStockId()).orElse(null);
            if (stock == null) {
                throw new Exception("Stock not found.");
            }

            rentalManage.setAccount(account);
            rentalManage.setExpectedRentalOn(rentalManageDto.getExpectedRentalOn());
            rentalManage.setExpectedReturnOn(rentalManageDto.getExpectedReturnOn());
            rentalManage.setStatus(rentalManageDto.getStatus());
            rentalManage.setStock(stock);

            rentalManage = setRentalStatusDate(rentalManage, rentalManageDto.getStatus());

            // データベースへの保存
            this.rentalManageRepository.save(rentalManage);
        } catch (Exception e) {
            throw e;
        }
    }

    // private RentalManage updateRentalStatusDate(RentalManage rentalManage, Integer status) {
    //     Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        
    //     if (status == RentalStatus.RENTAlING.getValue()) {
    //         rentalManage.setRentaledAt(timestamp);
    //     } else if (status == RentalStatus.RETURNED.getValue()) {
    //         rentalManage.setReturnedAt(timestamp);
    //     } else if (status == RentalStatus.CANCELED.getValue()) {
    //         rentalManage.setCanceledAt(timestamp);
    //     }

    //     return rentalManage;
    // }

    /**public void updateStatus(Long id, Integer status) {
        Optional<RentalManage> optionalRentalManage = rentalManageRepository.findById(id);
            RentalManage rentalManage = optionalRentalManage.get();
            Integer oldStatus = rentalManage.getStatus();
        
        if (oldStatus == 0) {            
            if (status == 1) {                
                // 貸出待ちから貸出中への状態遷移
                oldStatus = 1;             
            } else if (status == 3) {                
                // 貸出待ちからキャンセルへの状態遷移
                oldStatus = 3;             
            } else {                
                // 不正な状態遷移を検出した場合の処理
                System.out.println("不正な状態遷移です。");             
            }         
        } else if (oldStatus == 1) {            
            if (status == 2) {                
                // 貸出中から返却済みへの状態遷移
                oldStatus = 2;             
            } else {                
                // 不正な状態遷移を検出した場合の処理
                System.out.println("不正な状態遷移です。");             
            }         
        } else {            
            // その他の状態の場合の処理
            System.out.println("サポートされていない状態です。");         
        }     
    } */
}
