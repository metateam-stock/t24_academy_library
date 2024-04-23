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
import jp.co.metateam.library.model.RentalManage;
import jp.co.metateam.library.model.StockDto;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import jp.co.metateam.library.model.RentalManageDto;
import jp.co.metateam.library.model.Stock;
import jp.co.metateam.library.service.AccountService;
import jp.co.metateam.library.model.Account;
import jp.co.metateam.library.values.RentalStatus;
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
        //テーブルから持ってくる
        List<RentalManage> rentalManageList = this.rentalManageService.findAll();
        List<Stock> stockList= this.stockService.findStockAvailableAll();
        List<Account> accountList=this.accountService.findAll();
    
        //model.addAttribute("rentalManageList", rentalManageList);
        //在庫管理番号モデル
        model.addAttribute("stockList", stockList);
        //貸出ステータスモデル
        model.addAttribute("rentalStatus", RentalStatus.values());
        //社員番号モデル
        model.addAttribute("accounts",accountList);

        if (!model.containsAttribute("rentalManageDto")) {
            model.addAttribute("rentalManageDto", new RentalManageDto());
        }

        return "rental/add";
    }  

    @PostMapping("/rental/add")
    public String save(@Valid @ModelAttribute RentalManageDto RentalManageDto, BindingResult result, RedirectAttributes ra) {
        try {
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

}
