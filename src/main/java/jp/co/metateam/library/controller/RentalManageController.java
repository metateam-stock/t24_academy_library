package jp.co.metateam.library.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import jp.co.metateam.library.service.AccountService;
import jp.co.metateam.library.service.RentalManageService;
import jp.co.metateam.library.service.StockService;
import lombok.extern.log4j.Log4j2;
import java.util.Date;
import jp.co.metateam.library.model.RentalManage;
import java.util.List;
import org.springframework.web.bind.annotation.ModelAttribute;
import jp.co.metateam.library.model.BookMst;
import jp.co.metateam.library.model.Stock;
import jp.co.metateam.library.model.StockDto;
import java.time.LocalDate;
import java.util.Date;
import java.time.format.DateTimeFormatter;
import jp.co.metateam.library.model.BookMstDto;
import jp.co.metateam.library.model.AccountDto;
import jp.co.metateam.library.values.RentalStatus;
import jp.co.metateam.library.model.Account;
import jp.co.metateam.library.repository.AccountRepository;
import jp.co.metateam.library.model.RentalManageDto;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;
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
        // 貸出管理テーブルから全件取得
            List<RentalManage> rentalManageList = this.rentalManageService.findAll();
        // 貸出一覧画面に渡すデータをmodelに追加  
            model.addAttribute("rentalManageList", rentalManageList);
        // 貸出一覧画面に遷移
            return "rental/index";
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
    
                ra.addFlashAttribute("rentalManageDto", rentalManageDto);
                ra.addFlashAttribute("org.springframework.validation.BindingResult.rentalManageDto", result);
               return "redirect:/rental/add";   
            } 
        } 
} 

        


// @PostMapping("/stock/{id}/edit")
    // public String update(@PathVariable("id") String id, @Valid @ModelAttribute StockDto stockDto, BindingResult result, RedirectAttributes ra) {
    //     try {
    //         if (result.hasErrors()) {
    //             throw new Exception("Validation error.");
    //         }
    //         // 登録処理
    //         stockService.update(id, stockDto);

    //         return "stock/index";
    //     } catch (Exception e) {
    //         log.error(e.getMessage());

    //         ra.addFlashAttribute("stockDto", stockDto);
    //         ra.addFlashAttribute("org.springframework.validation.BindingResult.stockDto", result);

    //         return "redirect:/stock/edit";
    // @GetMapping("/stock/calendar")
    // public String calendar(@RequestParam(required = false) Integer year, @RequestParam(required = false) Integer month, Model model) {

    //     LocalDate today = year == null || month == null ? LocalDate.now() : LocalDate.of(year, month, 1);
    //     Integer targetYear = year == null ? today.getYear() : year;
    //     Integer targetMonth = today.getMonthValue();

    //     LocalDate startDate = LocalDate.of(targetYear, targetMonth, 1);
    //     Integer daysInMonth = startDate.lengthOfMonth();

    //     List<Object> daysOfWeek = this.stockService.generateDaysOfWeek(targetYear, targetMonth, startDate, daysInMonth);
    //     List<String> stocks = this.stockService.generateValues(targetYear, targetMonth, daysInMonth);

    //     model.addAttribute("targetYear", targetYear);
    //     model.addAttribute("targetMonth", targetMonth);
    //     model.addAttribute("daysOfWeek", daysOfWeek);
    //     model.addAttribute("daysInMonth", daysInMonth);

    //     model.addAttribute("stocks", stocks);

    //     return "stock/calendar";
    // 


