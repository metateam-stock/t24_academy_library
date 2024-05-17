package jp.co.metateam.library.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import jp.co.metateam.library.model.Account;
import jp.co.metateam.library.model.BookMst;
import jp.co.metateam.library.model.RentalManage;
import jp.co.metateam.library.model.RentalManageDto;
import jp.co.metateam.library.model.Stock;
import jp.co.metateam.library.model.StockDto;
import jp.co.metateam.library.service.AccountService;
import jp.co.metateam.library.service.RentalManageService;
import jp.co.metateam.library.service.StockService;
import jp.co.metateam.library.values.RentalStatus;
import jp.co.metateam.library.values.StockStatus;
import lombok.extern.log4j.Log4j2;

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
        return "rental/index";
    }

    @GetMapping("/rental/add")
    public String add(Model model) {

        List<Stock> stockList = this.stockService.findAll();
        List<Account> accounts = this.accountService.findAll();

        model.addAttribute("accounts", accounts);
        model.addAttribute("stockList", stockList);
        model.addAttribute("rentalStatus", RentalStatus.values());

        if (!model.containsAttribute("rentalManageDto")) {
            model.addAttribute("rentalManageDto", new RentalManageDto());
        }

        return "rental/add";
    }



/**
 * @param rentalManageDto
 * @param result
 * @param ra
 * @return
 */
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
 
            ra.addFlashAttribute("rentalManageDto", rentalManageDto);
            ra.addFlashAttribute("org.springframework.validation.BindingResult.rentalManageDto", result);
 
            return "redirect:/rental/add";
        }
    }

/**
 * @param id
 * @param model
 * @return
 */
@GetMapping("/rental/{id}/edit")
    public String edit(@PathVariable("id") String id, Model model) {
        List<Account> accounts = this.accountService.findAll();
        List<Stock> stockList = this.stockService.findStockAvailableAll();
        //model.addAttribute（第一引数、第二引数);第二引数のデータを第一引数の型、箱に入れてView（HTML）に渡す
        //<option th:each="account : ${accounts}" th:value="${account.employeeId}"
        model.addAttribute("accounts", accounts);
        //<option th:each="stock : ${stockList}" th:value="${stock.id}"
        model.addAttribute("stockList", stockList);
        //<option th:each="status : ${rentalStatus}" th:value="${status.value}"
        model.addAttribute("rentalStatus", RentalStatus.values());
        //ifのブロックは、編集を行いたい貸出管理番号に設定された情報（社員番号、貸出予定日、返却予定日、在庫管理番号、貸出ステータス）ダミーデータをセットする
        if (!model.containsAttribute("rentalManage")) {
            //クラス名 変数名 = new クラス名(); →インスタンスの生成
            RentalManage rentalManage = this.rentalManageService.findById(Long.valueOf(id));
            RentalManageDto rentalManageDto = new RentalManageDto();
 
            rentalManageDto.setId(rentalManage.getId());
            rentalManageDto.setStockId(rentalManage.getStock().getId());
            rentalManageDto.setEmployeeId(rentalManage.getAccount().getEmployeeId());
            rentalManageDto.setStatus(rentalManage.getStatus());
            rentalManageDto.setExpectedRentalOn(rentalManage.getExpectedRentalOn());
            rentalManageDto.setExpectedReturnOn(rentalManage.getExpectedReturnOn());
 
            model.addAttribute("rentalManage", rentalManageDto);
        }
 
        return "rental/edit";
    }

@PostMapping("/rental/{id}/edit")
public String update(@PathVariable("id") String id, @Valid @ModelAttribute RentalManageDto rentalManageDto, BindingResult result, RedirectAttributes ra)throws Exception {
    try {
        //変更前情報を取得
        RentalManage rentalManage = this.rentalManageService.findById(Long.valueOf(id));

        //変更後のステータスを渡してDtoでバリデーションチェック
        String validationError = rentalManageDto.validationChecks(rentalManage.getStatus());
        if(validationError != null){
            result.addError(new FieldError("rentalManage", "status", validationError));
        } 

        //バリデーションエラーがあるかを判別。エラーあり：例外を投げる エラーなし：登録処理に移る
        if (result.hasErrors()) {
            throw new Exception("Validation error.");
        }
        // 登録処理
        Long rentalManageId = Long.valueOf(id);//rentalManageIdをLong型に変換
        rentalManageService.update(rentalManageId, rentalManageDto);//貸出情報の更新

        return "redirect:/rental/index";
    //エラーが発生した場合、入力されたデータとバリデーション結果を貸出編集画面に渡す
     } catch (Exception e) {
         log.error(e.getMessage());//ログにエラーメッセージを記録
        
         ra.addFlashAttribute("rentalManage", rentalManageDto);
         ra.addFlashAttribute("org.springframework.validation.BindingResult.rentalManage", result);

         return "redirect:/rental/" + id +"/edit";
     }
}
}