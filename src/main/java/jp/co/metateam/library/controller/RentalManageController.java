package jp.co.metateam.library.controller;
import java.security.Timestamp;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import java.util.Optional;
import jakarta.validation.Valid;
import jp.co.metateam.library.model.Account;
import jp.co.metateam.library.model.AccountDto;
import jp.co.metateam.library.model.Stock;
import jp.co.metateam.library.model.StockDto;
import jp.co.metateam.library.service.AccountService;
import jp.co.metateam.library.service.RentalManageService;
import jp.co.metateam.library.service.StockService;
import jp.co.metateam.library.model.RentalManageDto;
import jp.co.metateam.library.values.RentalStatus;
import lombok.extern.log4j.Log4j2;
import jp.co.metateam.library.model.RentalManage;
import jp.co.metateam.library.values.RentalStatus;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.validation.FieldError;
/**
 * 貸出管理関連クラスß
 */
@Log4j2
@Controller
 public class RentalManageController {

    private final AccountService accountService;
    private final RentalManageService rentalManageService;
    private final StockService stockService;

    @Autowired
    public RentalManageController(
        AccountService accountService, 
        RentalManageService rentalManageService, 
        StockService stockService
    ) {
        this.accountService = accountService;
        this.rentalManageService = rentalManageService;
        this.stockService = stockService;
    }

    /**
     * 貸出一覧画面初期表示
     * @param model
     * @return
     */
    @GetMapping("/rental/index")
    public String index(Model model) {
        // 貸出管理テーブルから全件取得
        List <RentalManage> rentalManageList = this.rentalManageService.findAll();
        // 貸出一覧画面に渡すデータをmodelに追加
        model.addAttribute("rentalManageList", rentalManageList);
        //貸出一覧画面に遷移
        return "rental/index";

    
    
    
    }


@GetMapping("/rental/add") 
public String add(@RequestParam(required = false) String stockId, @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date expectedRentalOn, Model model) {//リンク押下した時の情報を取得
    List <Account> accounts = this.accountService.findAll();
    List <Stock> stockList = this.stockService.findAll();
        
    model.addAttribute("rentalStatus", RentalStatus. values());
    model.addAttribute("stockList", stockList);
    model.addAttribute("accounts",accounts);
    if (!model.containsAttribute("rentalManageDto")) { 
      RentalManageDto rentalManageDto = new RentalManageDto();//新しいrentalmanageDtoを作る  
        if(stockId !=null && expectedRentalOn !=null){
        rentalManageDto.setStockId(stockId);
        rentalManageDto.setExpectedRentalOn(expectedRentalOn);


    }
    model.addAttribute("rentalManageDto", rentalManageDto);
}
    return "rental/add";   
    }        
        
        @PostMapping("/rental/add") 
        public String save(@Valid @ModelAttribute RentalManageDto rentalManageDto, BindingResult result, RedirectAttributes ra) { 
            try { 
                if (result.hasErrors()) { 
                    throw new Exception("Validation error."); 
                } 
                String errorMessage = checkInventoryStatus(rentalManageDto.getStockId());
                if (errorMessage !=null){
                    result.addError(new FieldError("rentalManageDto","stockId",errorMessage));
                    throw new Exception("Validation error."); 
                }
                String errorText = Datecheck (rentalManageDto,rentalManageDto.getStockId());
                if (errorText != null){
                    result.addError(new FieldError("rentalManageDto","expectedRentalOn",errorText));
                    result.addError(new FieldError("rentalManageDto","expectedReturnOn",errorText));
                    throw new Exception("Validation error."); 
                }
                // if (result.hasErrors()) { 
                //     throw new Exception("Validation error."); 
                // } 
    
                // 登録処理 
    
                this.rentalManageService.save(rentalManageDto); 
    
                return "redirect:/rental/index"; 
            } catch (Exception e) { 
                log.error(e.getMessage()); 
    
                ra.addFlashAttribute("rentalManageDto", rentalManageDto); 
                ra.addFlashAttribute("org.springframework.validation.BindingResult.rentalManageDto", result); 
    
            return "redirect:/rental/add"; 
            }
        }
    
        @GetMapping("/rental/{id}/edit")
        public String edit(@PathVariable("id") Long id, Model model) {
            List <Account> accountList = this.accountService.findAll();
            List <Stock> stockList = this.stockService.findAll();
    
           model.addAttribute("rentalStatus", RentalStatus.values());
           model.addAttribute("accounts", accountList);
           model.addAttribute("stockList", stockList);
    
            if (!model.containsAttribute("rentalManageDto")) {
                RentalManageDto rentalManageDto = new RentalManageDto();
                RentalManage rentalManage = this.rentalManageService.findById(id);
    
                rentalManageDto.setId(rentalManage.getId());
                rentalManageDto.setAccount(rentalManage.getAccount());
                rentalManageDto.setExpectedRentalOn(rentalManage.getExpectedRentalOn());
                rentalManageDto.setExpectedReturnOn(rentalManage.getExpectedReturnOn());
                rentalManageDto.setStock(rentalManage.getStock());
                rentalManageDto.setStatus(rentalManage.getStatus());
                rentalManageDto.setEmployeeId(rentalManage.getAccount().getEmployeeId());
                rentalManageDto.setStockId(rentalManage.getStock().getId());
                model.addAttribute("rentalManageDto", rentalManageDto );
            }
    
            return "rental/edit";
        }
    
    
    
    @PostMapping("/rental/{id}/edit")
    public String update(@PathVariable("id") Long id, @Valid @ModelAttribute RentalManageDto rentalManageDto, BindingResult result, RedirectAttributes ra, Model model) {
        try {
            if (result.hasErrors()) {
                throw new Exception("Validation error.");
            }
    
            RentalManage rentalManage = this.rentalManageService.findById(id);
            int status = rentalManage.getStatus();
            int newStatus = rentalManageDto.getStatus();
            LocalDateTime ldt = LocalDateTime.now();
            Date expectedRentalOn = rentalManageDto.getExpectedRentalOn();
            Date expectedReturnOn = rentalManageDto.getExpectedReturnOn();
    
            LocalDateTime expectedRentalOnLdt = Instant.ofEpochMilli(expectedRentalOn.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
            LocalDateTime expectedReturnOnLdt = Instant.ofEpochMilli(expectedReturnOn.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
            Optional<String> dayError = rentalManageDto.ValidDateTime(expectedRentalOn,expectedReturnOn);
 
            if(dayError.isPresent()){
                FieldError fieldError = new FieldError("rentalManageDto","expectedReturnOn", dayError.get());
                //dateErrorから取得したエラーメッセージをfieldErrorに入れる
                result.addError(fieldError);
                //resultにエラーの情報を入れる
                throw new Exception("Validation error");
                //エラーを投げる
            }
            if (newStatus == 1 && ldt.isBefore(expectedRentalOnLdt)) {
                FieldError fieldError = new FieldError("rentalManageDto", "expectedRentalOn", "貸出予定日が未来のためこのステータスは選択できません");
                result.addError(fieldError);
                throw new Exception("Validation error.");
    
            } else if (newStatus == 2 && ldt.isBefore(expectedReturnOnLdt)) {
                FieldError fieldError = new FieldError("rentalManageDto", "expectedReturnOn", "返却予定日が未来のためこのステータスは選択できません");
                    result.addError(fieldError);
                    throw new Exception("Validation error.");
    
            } else if ((status == newStatus) || (status == 0 && (newStatus == 1 || newStatus == 3)) || (status == 1 && newStatus == 2)) {
                this.rentalManageService.update(id, rentalManageDto);
    
            } else {
                FieldError fieldError = new FieldError("rentalManageDto", "status", "このステータスは選択できません");
                result.addError(fieldError);
            throw new Exception("Validation error.");
            }
    
            return "redirect:/rental/index";
    
        } catch (Exception e) {
            log.error(e.getMessage());
    
             RentalManage rentalManage = this.rentalManageService.findById(id);
    
             rentalManageDto.setId(rentalManage.getId());
             rentalManageDto.setAccount(rentalManage.getAccount());
             rentalManageDto.setStock(rentalManage.getStock());
             rentalManageDto.setStatus(rentalManage.getStatus());
    
    
            ra.addFlashAttribute("rentalManageDto", rentalManageDto);
            ra.addFlashAttribute("org.springframework.validation.BindingResult.rentalManageDto", result);
    
           return String.format("redirect:/rental/%s/edit", id);
        }
    }
    private String checkInventoryStatus(String id) {
        // 在庫ステータスを確認するロジックを記述
        Stock stock = this.stockService.findById(id);
            if (stock.getStatus() == 0) {
                return null; // 利用可の場合はエラーメッセージなし
            } else {
                return "この本は利用できません"; // 利用不可の場合はエラーメッセージを返す
            }
    }
    
    public String Datecheck(RentalManageDto rentalManageDto,String id){
        List<RentalManage> rentalAvailable = this.rentalManageService.findByStockIdAndStatusIn(id);
        if (rentalAvailable != null) {
            // ループ処理
            for (RentalManage rentalManage : rentalAvailable) {
                if (rentalManage.getExpectedReturnOn().after(rentalManageDto.getExpectedRentalOn())
                        && rentalManage.getExpectedRentalOn().before(rentalManageDto.getExpectedReturnOn())) {
                    return "貸出期間が重複しています";
                }
            }
            return null;
        } else {
            return null;
        }
    }
    public String Datecheck(RentalManageDto rentalManageDto,String id,Long rentalId){
        List <RentalManage> rentalManages = this.rentalManageService.findByStockIdAndStatusIn(id, rentalId);
        if (rentalManages != null) {
            // ループ処理
            for (RentalManage rentalManage : rentalManages) {
                if (rentalManage.getExpectedReturnOn().after(rentalManageDto.getExpectedRentalOn())
                        && rentalManage.getExpectedRentalOn().before(rentalManageDto.getExpectedReturnOn())) {
                    return "貸出期間が重複しています";
                }
            }
            return null;
        } else {
            return null;
        }
    } 
}
    