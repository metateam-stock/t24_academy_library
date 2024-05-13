package jp.co.metateam.library.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jp.co.metateam.library.service.AccountService;
import jp.co.metateam.library.service.RentalManageService;
import jp.co.metateam.library.service.StockService;
import lombok.extern.log4j.Log4j2;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jp.co.metateam.library.model.RentalManage;
import jp.co.metateam.library.model.StockDto;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import jp.co.metateam.library.model.RentalManageDto;
import jp.co.metateam.library.model.Stock;
import jp.co.metateam.library.service.AccountService;
import jp.co.metateam.library.model.Account;
import jp.co.metateam.library.model.BookMst;
import jp.co.metateam.library.values.RentalStatus;
import jp.co.metateam.library.values.StockStatus;

import java.util.List;

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
        List<RentalManage> rentalManageList = this.rentalManageService.findAll();
        // 貸出一覧画面に渡すデータをmodelに追加
        model.addAttribute("rentalManageList", rentalManageList);
        // 貸出一覧画面に遷移
        return "/rental/index";
    }

    @GetMapping("/rental/add")
    public String add(Model model) {
      
        //在庫が利用可能な在庫テーブルからデータを取得
        List<Stock> stockList= this.stockService.findStockAvailableAll();
        //アカウントテーブルから全件取得
        List<Account> accountList=this.accountService.findAll();
    
        //在庫管理番号のデータをmodelに追加
        model.addAttribute("stockList", stockList);
        //貸出ステータスのデータをmodelに追加
        model.addAttribute("rentalStatus", RentalStatus.values());
        //社員番号のデータをmodelに追加
        model.addAttribute("accounts",accountList);
        //rentalMnageDtoに存在しなかったら、rentalMnageDtoに新しく登録？
        if (!model.containsAttribute("rentalManageDto")) {
            model.addAttribute("rentalManageDto", new RentalManageDto());
        }

        return "rental/add";
    }  

    @PostMapping("/rental/add")
    public String save(@Valid @ModelAttribute RentalManageDto RentalManageDto, BindingResult result, RedirectAttributes ra) {
        try {//エラーがあったらバリデーションエラーを表示
            if (result.hasErrors()) {
                throw new Exception("Validation error.");
            }
            // 登録処理
            this.rentalManageService.save(RentalManageDto);

            return "redirect:/rental/index";
        } catch (Exception e) {
            log.error(e.getMessage());

            ra.addFlashAttribute("RentalManageSto", RentalManageDto);
            ra.addFlashAttribute("org.springframework.validation.BindingResult.RentalManageDto", result);

            return "redirect:/rental/add";
        }
    }
    //貸出編集画面のHTMLとControllerの画面をつなぐGETメソッドコード
     @GetMapping("/rental/{id}/edit")
    public String edit(@PathVariable("id") String id, Model model) {
        List<Account> accountList=this.accountService.findAll();
        List<Stock> stockList= this.stockService.findStockAvailableAll();
        
        model.addAttribute("stockList", stockList);
        model.addAttribute("rentalStatus", RentalStatus.values());
        model.addAttribute("accounts",accountList);
        
        RentalManage rentalManage = this.rentalManageService.findById(Long.valueOf(id));

        if (!model.containsAttribute("rentalManageDto")) {
            RentalManageDto rentalManageDto= new RentalManageDto();
           
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
    //BindingResult resultにRentalManageDtoでの入力チェックを行った情報を入れている
    public String update(@PathVariable("id") String id, @Valid @ModelAttribute RentalManageDto rentalManageDto, BindingResult result, RedirectAttributes ra, Model model) {
        try {
            RentalManage rentalManage=this.rentalManageService.findById(Long.valueOf(id));
            String validerro=rentalManageDto.isStatusError(rentalManage.getStatus());
            if(validerro != null){
            result.addError(new FieldError("rentalManageDto","status",validerro));
            }
            //「resultにエラーがある」がtrueだったら
            if (result.hasErrors()) {
                throw new Exception("Validation error.");
            }
            //在庫ステータスの利用可否
            
            // 登録処理
            rentalManageService.update(Long.valueOf(id), rentalManageDto);

            return "redirect:/rental/index";
        } catch (Exception e) {
            log.error(e.getMessage());

            //エラー表示後にもリストにデータを入れる必要がある

            ra.addFlashAttribute("rentalManageDto", rentalManageDto);
            ra.addFlashAttribute("org.springframework.validation.BindingResult.stockDto", result);

            List<Account> accountList=this.accountService.findAll();
            List<Stock> stockList= this.stockService.findStockAvailableAll();
        
            model.addAttribute("stockList", stockList);
            model.addAttribute("rentalStatus", RentalStatus.values());
            model.addAttribute("accounts",accountList);

            return "/rental/edit";
        }
    }

    

}
