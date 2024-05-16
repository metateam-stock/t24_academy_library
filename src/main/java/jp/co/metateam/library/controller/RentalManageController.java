package jp.co.metateam.library.controller;

import java.util.List;
import jp.co.metateam.library.model.RentalManage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import jp.co.metateam.library.service.AccountService;
import jp.co.metateam.library.service.RentalManageService;
import jp.co.metateam.library.service.StockService;
import lombok.extern.log4j.Log4j2;

import jp.co.metateam.library.model.Account;
import jp.co.metateam.library.model.Stock;
import jp.co.metateam.library.model.StockDto;
import jp.co.metateam.library.model.RentalManageDto;
import jp.co.metateam.library.values.RentalStatus;
import jp.co.metateam.library.values.StockStatus;
import jp.co.metateam.library.model.BookMst;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import ch.qos.logback.core.status.Status;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;



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
        List <RentalManage> rentalManageList = this.rentalManageService.findAll();
        // 貸出管理テーブルから全件取得
        model.addAttribute("rentalManageList",rentalManageList);
        // 貸出一覧画面に渡すデータをmodelに追加
        return "rental/index";
        // 貸出一覧画面に遷移
    }
    
    @GetMapping("/rental/add")
    public String add(Model model) {
        List<Account> accounts = this.accountService.findAll();
        List<Stock> stockList = this.stockService.findAll();
        

        model.addAttribute("accounts", accounts);
        model.addAttribute("stockList", stockList);
        model.addAttribute("rentalStatus", RentalStatus.values());
        

        if (!model.containsAttribute("rentalManageDto")) {
            model.addAttribute("rentalManageDto", new RentalManageDto());
        }

        return "rental/add";
    }

    @PostMapping("/rental/add")
    public String save(@Valid @ModelAttribute RentalManageDto rentalManageDto, BindingResult result, RedirectAttributes ra) {
        try {
            if (result.hasErrors()) {
                throw new Exception("Validation error.");
            }
            // 登録処理
            this.rentalManageService.save(rentalManageDto);

            return "redirect:/rental/index";
        } catch (Exception e) {
            log.error(e.getMessage());
            ra.addFlashAttribute("RentalManageDto", rentalManageDto);
            ra.addFlashAttribute("org.springframework.validation.BindingResult.RentalManageDto", result);
            return "redirect:/rental/add";
        }
    }
    
    //貸出編集
    @GetMapping("/rental/{id}/edit")
    //URIパスが /rental/{id}/edit であるGETリクエストを処理すること
    public String edit(@PathVariable("id") String id, Model model) {
        // このメソッドのシグネチャは、パス変数 id を受け取り、ビューにデータを渡すための Model オブジェクトを使用
        List<Account> accounts = this.accountService.findAll();
        List <Stock> stockList = this.stockService.findStockAvailableAll();
        // アカウントと在庫のリストを取得。accountService と stockService は、それぞれ findAll() と findStockAvailableAll() メソッドを介してこれらのデータを提供
 
        model.addAttribute("accounts", accounts);
        model.addAttribute("stockList", stockList);
        model.addAttribute("rentalStatus", RentalStatus.values());
        // モデルにアカウントのリスト、在庫のリスト、および RentalStatus 列挙型の値を追加
 
        if (!model.containsAttribute("rentalManage")) {
            RentalManage rentalManage = this.rentalManageService.findById(Long.valueOf(id));
            RentalManageDto rentalManageDto = new RentalManageDto();
            // モデルに "rentalManage" 属性が含まれていない場合の処理。これにより、再度ビューに移動する際に RentalManage オブジェクトをモデルに追加
 
            rentalManageDto.setId(rentalManage.getId());
            rentalManageDto.setStockId(rentalManage.getStock().getId());
            rentalManageDto.setEmployeeId(rentalManage.getAccount().getEmployeeId());
            rentalManageDto.setStatus(rentalManage.getStatus());
            rentalManageDto.setExpectedRentalOn(rentalManage.getExpectedRentalOn());
            rentalManageDto.setExpectedReturnOn(rentalManage.getExpectedReturnOn());
 
            model.addAttribute("rentalManage", rentalManageDto);
        }
 
        return "rental/edit";
        // 画面表示（HTML）"rental/edit" が返される。
    }
    

    @PostMapping("/rental/{id}/edit")
    public String update(@PathVariable("id") String id, @Valid @ModelAttribute RentalManageDto rentalManageDto, BindingResult result, RedirectAttributes ra)throws Exception {
        try {
     
            //変更前情報を取得
            RentalManage rentalManage = this.rentalManageService.findById(Long.valueOf(id));
            //変更後のステータスを渡してDtoでバリデーションチェック
            String validationError = rentalManageDto.validationCheck(rentalManage.getStatus());
           
            if(validationError != null){
                result.addError(new FieldError("rentalManage", "status", validationError));
            }
     
            //バリデーションエラーがあるかを判別。エラーあり：例外を投げる エラーなし：登録処理に移る
            if (result.hasErrors()) {
                throw new Exception("Validation error.");
            }
            // 登録処理
            Long rentalManageId = Long.valueOf(id);
            rentalManageService.update(rentalManageId, rentalManageDto);
     
            return "redirect:/rental/index";
        //エラーが発生すると入力したデータはDBに登録されずに編集画面に返す
         } catch (Exception e) {
             log.error(e.getMessage());
             ra.addFlashAttribute("rentalManage", rentalManageDto);
             ra.addFlashAttribute("org.springframework.validation.BindingResult.rentalManage", result);
     
             return "redirect:/rental/" + id +"/edit";
         }
    }

}


