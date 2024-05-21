package jp.co.metateam.library.service;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
import jp.co.metateam.library.repository.AccountRepository;
import jp.co.metateam.library.repository.RentalManageRepository;
import jp.co.metateam.library.repository.StockRepository;
import jp.co.metateam.library.values.RentalStatus;
import jp.co.metateam.library.model.RentalManageDto;
import jp.co.metateam.library.values.StockStatus;


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
    @Transactional
    public void update(Long id, RentalManageDto rentalManageDto) throws Exception {
        try {
            // 既存レコード取得
            RentalManage updateTargetBook = this.rentalManageRepository.findById(id).orElse(null);
            Account account = this.accountRepository.findByEmployeeId(rentalManageDto.getEmployeeId()).orElse(null);
            Stock stock =this.stockRepository.findById(rentalManageDto.getStockId()).orElse(null);


            if (updateTargetBook == null) {
                throw new Exception("RentalManage record not found.");
            }
            if (account == null) {
                throw new Exception("Account record not found.");
            }
            if (stock == null) {
                throw new Exception("Stock record not found.");
            }

            updateTargetBook.setId(rentalManageDto.getId());
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
    public String isStatusError(Integer beforeStatus, Integer afterStatus, LocalDate newexpectedRentalOn, LocalDate currentDate) {
        if(beforeStatus == RentalStatus.RETURNED.getValue() || beforeStatus == RentalStatus.CANCELED.getValue()) {
            return "キャンセル・返却済みから変更することはできません";
            //貸出待ちから返却済みに変更できない
        }else if(beforeStatus == RentalStatus.RENT_WAIT.getValue() && afterStatus == RentalStatus.RETURNED.getValue()){
            return "このステータスは無効です";
        //貸出中から貸出待ちに変更できない
        } else if(beforeStatus == RentalStatus.RENTAlING.getValue() && afterStatus == RentalStatus.RENT_WAIT.getValue()) {
            return "このステータスは無効です";
        //貸出中からキャンセルに変更できない
        } else if(beforeStatus == RentalStatus.RENTAlING.getValue() && afterStatus == RentalStatus.CANCELED.getValue()){
            return "このステータスは無効です";
         //貸出待ちから貸出中は当日でないとに変更できない
        } else if((!newexpectedRentalOn.isEqual(currentDate) && beforeStatus == RentalStatus.RENT_WAIT.getValue() && afterStatus == RentalStatus.RENTAlING.getValue())){
            return "本日以外の日付で、貸出中に変更することはできません";
         //貸出待ちから貸出中に変更するのは、貸出予定日当日である必要がある
        }
        return null;

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
    public Optional<String> rentalAble(String stockId, Date expected_return_on, Date expected_rental_on){
        if(rentalManageRepository.count(stockId) == 0) {
            return Optional.of("この本は貸し出せません");
        }
        if(rentalManageRepository.addtest(stockId) != rentalManageRepository.addwhetherDay( stockId, expected_return_on, expected_rental_on)){
            return Optional.of("この本は貸し出せません");
        }
        return Optional.empty();
    } 
    @Transactional
    public Optional<String> rentaleditAble(String stockId,Long id, Date expected_return_on, Date expected_rental_on){
        if(rentalManageRepository.count(stockId) == 0) {
            return Optional.of("この本は貸し出せません");
        }
        if(rentalManageRepository.whetherDay( stockId, id, expected_return_on, expected_rental_on) != rentalManageRepository.test(stockId, id)){
            return Optional.of("この本は貸し出せません");
        }
        return Optional.empty();
    }   
}
