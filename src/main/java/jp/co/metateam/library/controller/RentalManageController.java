package jp.co.metateam.library.controller;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import jp.co.metateam.library.service.AccountService;
import jp.co.metateam.library.service.RentalManageService;
import jp.co.metateam.library.service.StockService;
import lombok.extern.log4j.Log4j2;
import jp.co.metateam.library.model.RentalManage;
import jp.co.metateam.library.model.RentalManageDto;
import jp.co.metateam.library.values.RentalStatus;
import jp.co.metateam.library.model.Stock;
import jp.co.metateam.library.model.StockDto;
import jp.co.metateam.library.model.Account;
import jp.co.metateam.library.model.BookMstDto;
import jp.co.metateam.library.service.BookMstService;


/**
 * 貸出管理関連クラスß
 */
@Log4j2
@Controller
public class RentalManageController {

    private final AccountService accountService;
    private final RentalManageService rentalManageService;
    private final StockService stockService;
    private final BookMstService bookMstService;

    @Autowired
    public RentalManageController(
        AccountService accountService, 
        RentalManageService rentalManageService, 
        StockService stockService,
        BookMstService bookMstService
    ) {
        this.accountService = accountService;
        this.rentalManageService = rentalManageService;
        this.stockService = stockService;
        this.bookMstService = bookMstService;
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

        model.addAttribute("rentalStatus", RentalStatus.values());
        model.addAttribute("accounts", accountList);
        model.addAttribute("stockList", stockList);

        if (!model.containsAttribute("rentalManageDto")) {
            model.addAttribute("rentalManageDto", new RentalManageDto());
        }

        return "rental/add";
    }

    @PostMapping("/rental/add")
    public String save(@Valid @ModelAttribute RentalManageDto rentalManageDto, BindingResult result, RedirectAttributes ra, Model model) {
        try {
            if (result.hasErrors()) {
                throw new Exception("Validation error.");
            }
            //SQLに追加
            Long log = rentalManageService.countByStockIdAndStatusIn(rentalManageDto.getStockId());
            Long loggic = rentalManageService.countByStockIdAndStatusAndTermsIn(rentalManageDto.getStockId(), rentalManageDto.getExpectedRentalOn(), rentalManageDto.getExpectedReturnOn());

            // 登録処理
            this.rentalManageService.save(rentalManageDto);

            return "redirect:/rental/index";
        } catch (Exception e) {
            log.error(e.getMessage());

            List<Account> accountList = this.accountService.findAll();
            List<Stock> stockList = this.stockService.findStockAvailableAll();

            model.addAttribute("rentalStatus", RentalStatus.values());
            model.addAttribute("accounts", accountList);
            model.addAttribute("stockList", stockList);

            ra.addFlashAttribute("rentalManageDto", rentalManageDto);
            ra.addFlashAttribute("org.springframework.validation.BindingResult.rentalManageDto", result);

            return "redirect:/rental/add";
        }
    }

    
    @GetMapping("/rental/{id}/edit")
    public String edit(@PathVariable("id") Long id, Model model) {
        
        List<Account> accountList = this.accountService.findAll();
        List<Stock> stockList = this.stockService.findStockAvailableAll();

        model.addAttribute("rentalStatus", RentalStatus.values());
        model.addAttribute("accounts", accountList);
        model.addAttribute("stockList", stockList);
        
        if (!model.containsAttribute("rentalManageDto")) {
            model.addAttribute("rentalManageDto", new RentalManageDto());

        RentalManageDto rentalManageDto = new RentalManageDto();
        RentalManage rentalManage = this.rentalManageService.findById(Long.valueOf(id));

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
    public String update(@PathVariable("id") Long id, @Valid @ModelAttribute RentalManageDto rentalManageDto, BindingResult result, RedirectAttributes ra, Model model) {
        try {

            RentalManage rentalManage = this.rentalManageService.findById(Long.valueOf(id));
            String validerror = rentalManageDto.isValidStatus(rentalManage.getStatus());
            //ステータスチェック
            if(validerror != null){
                result.addError(new FieldError("rentalManageDto", "status", validerror));
            }
            
            
            //追加箇所
            //貸出可否チェック
            String stockId = rentalManageDto.getStockId();
            Long ID = rentalManageDto.getId();
            Integer status = rentalManageDto.getStatus();
            //SQL
            //ステータスが貸出待ちか貸出中のものを持ってくる
            Long stockcount = this.rentalManageService.countByStockIdAndStatusInAndIdNot(stockId, id);
            //0または1かの判定
            if(status == 0 || status == 1) {
                
                //利用可か利用不可か
                if(!(stockcount == 0)) {
                    //重複件数
                    Date expectedRentalOn = rentalManageDto.getExpectedRentalOn();
                    Date expectedReturnOn = rentalManageDto.getExpectedReturnOn();
                    //SQL
                    Long rentalcount = this.rentalManageService.countByStockIdAndStatusAndIdNotAndTermsIn(stockId, id, expectedReturnOn, expectedRentalOn);


                    //日付重複チェック
                    if(!(stockcount == rentalcount)){
                        String rentalError = "この書籍は貸出できません";
                        result.addError(new FieldError("rentalManageDto", "expectedRentalOn", rentalError));
                        result.addError(new FieldError("rentalManageDto", "expectedReturnOn", rentalError));
                        throw new Exception("StockStatus record not found,");
                    }                    
                }
            }
            
            if (result.hasErrors()) {
                throw new Exception("Validation error.");
            }
            

            // 登録処理
            this.rentalManageService.update(Long.valueOf(id), rentalManageDto);

            return "redirect:/rental/index";
        } catch (Exception e) {
            log.error(e.getMessage());

            ra.addFlashAttribute("rentalManageDto", rentalManageDto);
            ra.addFlashAttribute("org.springframework.validation.BindingResult.rentalManageDto", result);
            
            List<Account> accountList = this.accountService.findAll();
            List<Stock> stockList = this.stockService.findAll();

            model.addAttribute("rentalStatus", RentalStatus.values());
            model.addAttribute("accounts", accountList);
            model.addAttribute("stockList", stockList);
            model.addAttribute("stockId",this.rentalManageService.findById(Long.valueOf(id)).getStock().getId());

            return "rental/edit";
        }
    }
}



