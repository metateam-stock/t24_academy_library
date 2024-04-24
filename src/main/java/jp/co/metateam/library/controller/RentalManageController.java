package jp.co.metateam.library.controller;
 
import java.util.List;
 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
 
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import jp.co.metateam.library.service.AccountService;
import jp.co.metateam.library.service.RentalManageService;
import jp.co.metateam.library.service.StockService;
import lombok.extern.log4j.Log4j2;
 
import jp.co.metateam.library.model.Account;
import jp.co.metateam.library.model.AccountDto;
import jp.co.metateam.library.model.Stock;
import jp.co.metateam.library.model.StockDto;
import jp.co.metateam.library.model.RentalManage;
import jp.co.metateam.library.model.RentalManageDto;
import jp.co.metateam.library.service.RentalManageService;
import jp.co.metateam.library.values.RentalStatus;


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

     // 貸出管理テーブルから全件取得
    @GetMapping("/rental/index")
    public String index(Model model) {
        
        List<RentalManage> rentalManageList = this.rentalManageService.findAll();

        model.addAttribute("rentalManageList", rentalManageList);
   
        return "rental/index";
    }

// 貸出一覧画面に渡すデータをmodelに追加   
    @GetMapping("/rental/add")
    public String add(Model model) {

        List<Account> accounts = this.accountService.findAll();
<<<<<<< HEAD
        List<Stock> stockList = this.stockService.findStockAvailableAll();
=======
        List<Stock> stockList = this.stockService.findAll();
        List<RentalManage> rentalManage = this.rentalManageService.findAll();
>>>>>>> c5587e76af5408727e1315733ef49333fbe1e7a3

        model.addAttribute("accounts", accounts);
        model.addAttribute("stockList", stockList);
        model.addAttribute("rentalStatus", RentalStatus.values());

        if (!model.containsAttribute("rentalManageDto")) {
            model.addAttribute("rentalManageDto", new RentalManageDto());
        }

        return "rental/add";
    }

  
    @PostMapping("/rental/add")
    public String register(@ModelAttribute RentalManageDto rentalManageDto, BindingResult result, RedirectAttributes ra, Model model) {
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
            ra.addFlashAttribute("org.springframework.validation.BindingResult.rentalmanageDto", result);

            return "redirect/rental/add";
        }
    }
}


            
