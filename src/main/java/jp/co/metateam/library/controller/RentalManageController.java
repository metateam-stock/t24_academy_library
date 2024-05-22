package jp.co.metateam.library.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jp.co.metateam.library.service.AccountService;
import jp.co.metateam.library.service.RentalManageService;
import jp.co.metateam.library.service.StockService;
import lombok.extern.log4j.Log4j2;
import jp.co.metateam.library.model.RentalManageDto;
import jp.co.metateam.library.model.RentalManage;
import java.util.List;
import jp.co.metateam.library.values.RentalStatus;
import jp.co.metateam.library.model.AccountDto;
import org.springframework.web.bind.annotation.ModelAttribute;
import jakarta.validation.Valid;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PostMapping;
import jp.co.metateam.library.model.Account;
import jp.co.metateam.library.model.Stock;
import org.springframework.web.bind.annotation.PathVariable;
import jp.co.metateam.library.values.RentalStatus;
import java.util.Optional;
import java.util.Date;



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
        // 貸出一覧画面に遷移
        return "rental/index";
    }



    @GetMapping("/rental/add")
    public String add(Model model) {
        List<Account> accountList = this.accountService.findAll();
        List<Stock> stockList = this.stockService.findAll();

        model.addAttribute("accounts", accountList);
        model.addAttribute("stockList", stockList);
        model.addAttribute("rentalStatus", RentalStatus.values());

        if (!model.containsAttribute("rentalManageDto")) {
            model.addAttribute("rentalManageDto", new RentalManageDto());
        }

        return "rental/add";
    }

    
    @PostMapping("/rental/add")
    public String save(@Valid @ModelAttribute RentalManageDto rentalManageDto, BindingResult result, RedirectAttributes ra , Model model) {
        try {
            String rentalDataerror = this.rentalCheck(rentalManageDto, rentalManageDto.getStockId());

            
            if (rentalDataerror != null) {
                result.addError(new FieldError("rentalManageDto", "expectedRentalOn", rentalDataerror));
            }
            if (rentalDataerror != null) {
                result.addError(new FieldError("rentalManageDto", "expectedReturnOn", rentalDataerror));
            }
            if (result.hasErrors()) {
                throw new Exception("Validation error.");
            }
            
 
            // 登録処理
            this.rentalManageService.save(rentalManageDto);

            return "redirect:/rental/index";
        } catch (Exception e) {
            log.error(e.getMessage());

            //List<Account> accountList = this.accountService.findAll();

            //List<Stock> stockList = this.stockService.findAll();
 
            // model.addAttribute("accounts", accountList);

            // model.addAttribute("stockList", stockList);

            //model.addAttribute("rentalStatus", RentalStatus.values());
            ra.addFlashAttribute("rentalManageDto", rentalManageDto);
            ra.addFlashAttribute("org.springframework.validation.BindingResult.rentalManageDto", result);

            return "redirect:/rental/add";
        }
        
    }
    public String rentalCheck(RentalManageDto rentalManageDto, String id) {
        // 貸出管理のDBから在庫管理番号に紐づくレコードのうちステータスが0,1のものを全件取得しリストの格納
        List<RentalManage> rentalAvailable = this.rentalManageService.findByStockIdAndStatusIn2(id);
 
        if (rentalAvailable != null) {
 
            // 取得したレコードが０件の場合、noならループ、yesなら終了
            for (RentalManage List : rentalAvailable) {
                if (List.getExpectedRentalOn().before(rentalManageDto.getExpectedReturnOn())
                        && List.getExpectedReturnOn().after(rentalManageDto.getExpectedRentalOn())) {
                    return "この書籍は、入力された日付で貸出できません";
                }
            }
        }
        return null;
    }



    //貸出編集機能


    @GetMapping("/rental/{id}/edit")
    public String edit(@PathVariable("id") String id, Model model) {
        List<Account> accountList = this.accountService.findAll();
        List<Stock> stockList = this.stockService.findAll();

        model.addAttribute("accounts", accountList);
        model.addAttribute("stockList", stockList);
        model.addAttribute("rentalStatus", RentalStatus.values());

        RentalManage rentalManage = this.rentalManageService.findById(Long.valueOf(id));
        RentalManageDto rentalManageDto = new RentalManageDto(); 
        
        if (!model.containsAttribute("rentalManageDto")) {
            rentalManageDto.setId(rentalManage.getId()); 
            rentalManageDto.setEmployeeId(rentalManage.getAccount().getEmployeeId()); 
            rentalManageDto.setExpectedRentalOn(rentalManage.getExpectedRentalOn());
            rentalManageDto.setExpectedReturnOn(rentalManage.getExpectedReturnOn()); 
            rentalManageDto.setStockId(rentalManage.getStock().getId()); 
            rentalManageDto.setStatus(rentalManage.getStatus()); 

            model.addAttribute("rentalManageDto", rentalManageDto);
        }
        

    return "rental/edit";
    }
    @PostMapping("/rental/{id}/edit")
    public String update(@PathVariable("id") String id, @Valid @ModelAttribute RentalManageDto rentalManageDto, BindingResult result,  RedirectAttributes ra, Model model) {
        try {

            
            RentalManage rentalManage = this.rentalManageService.findById(Long.valueOf(id));
            Integer preStatus = rentalManage.getStatus(); // RentalManage オブジェクトからステータスを取得
            Integer newStatus = rentalManageDto.getStatus();
            Date expectedRentalOn = rentalManageDto.getExpectedRentalOn();
            Date expectedReturnOn = rentalManageDto.getExpectedReturnOn();
            String rentalDataerror = this.rentalCheck(rentalManageDto, rentalManageDto.getStockId(), rentalManageDto.getId());
 
            
            if (rentalDataerror != null) {
                result.addError(new FieldError("rentalManageDto", "expectedRentalOn", rentalDataerror));
            }
            if (rentalDataerror != null) {
                result.addError(new FieldError("rentalManageDto", "expectedReturnOn", rentalDataerror));
            }



            Optional<String> dateError = rentalManageDto.ValidDateTime(expectedRentalOn, expectedReturnOn);
            
            if(dateError.isPresent()){
                FieldError fieldError = new FieldError("rentalManageDto","expectedReturnOn",dateError.get());
                
                result.addError(fieldError);

                throw new Exception("Validation error.");
            }


            Optional<String> statusError = rentalManageDto.ValidStatus(preStatus, newStatus);

            if(statusError.isPresent()){
                FieldError fieldError = new FieldError("rentalManageDto","status",statusError.get());
                
                result.addError(fieldError);

                throw new Exception("Validation error.");
            }

            

        
            
        if (result.hasErrors()) {
            throw new Exception("Validation error.");
        }

            // 更新処理
            this.rentalManageService.update(Long.valueOf(id), rentalManageDto);
            
            return "redirect:/rental/index";

        } catch (Exception e) {
            log.error(e.getMessage());

            ra.addFlashAttribute("rentalManageDto", rentalManageDto);
            ra.addFlashAttribute("org.springframework.validation.BindingResult.rentalManageDto", result);

            String formattedId = String.format("%s", id);
            return "redirect:/rental/" + formattedId + "/edit";
        }
    }

    public String rentalCheck(RentalManageDto rentalManageDto, String id, Long rentalId) {
        // 貸出管理のDBから在庫管理番号に紐づくレコードのうちステータスが0,1のものを全件取得しリストの格納
        List<RentalManage> rentalAvailable = this.rentalManageService.findByStockIdAndStatusIn1(id,
                Long.valueOf(rentalId));
 
        if (rentalAvailable != null) {
 
            // 取得したレコードが０件の場合、noならループ、yesなら終了
            for (RentalManage List : rentalAvailable) {
                if (List.getExpectedRentalOn().before(rentalManageDto.getExpectedReturnOn())
                        && List.getExpectedReturnOn().after(rentalManageDto.getExpectedRentalOn())) {
                    return "この書籍は、入力された日付で貸出できません";
                }
            }
        }
        return null;
    }



}



