package jp.co.metateam.library.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jp.co.metateam.library.service.AccountService;
import jp.co.metateam.library.service.RentalManageService;
import jp.co.metateam.library.service.StockService;
import lombok.extern.log4j.Log4j2;
import jp.co.metateam.library.model.RentalManage;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import io.micrometer.common.util.StringUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jp.co.metateam.library.model.RentalManageDto;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import jp.co.metateam.library.values.RentalStatus;
import jp.co.metateam.library.model.Stock;
import jp.co.metateam.library.model.StockDto;
import jp.co.metateam.library.model.Account;
import jp.co.metateam.library.model.AccountDto;

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
            StockService stockService) {
        this.accountService = accountService;
        this.rentalManageService = rentalManageService;
        this.stockService = stockService;
    }

    /**
     * 貸出一覧画面初期表示
     * 
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

    @PostMapping("/rental/add")
    public String save(@Valid @ModelAttribute RentalManageDto rentalManageDto, BindingResult result,
            RedirectAttributes ra) {
        try {
            
            String rentalDateError = rentalCheck(rentalManageDto, rentalManageDto.getStockId());

            
            if (rentalDateError != null) {
                result.addError(new FieldError("rentalManageDto", "expectedRentalOn", rentalDateError));
            }
            if (rentalDateError != null) {
                result.addError(new FieldError("rentalManageDto", "expectedReturnOn", rentalDateError));
            }
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

    @GetMapping("/rental/{id}/edit")
    public String edit(@PathVariable("id") Long id, Model model) {
        List<Stock> stockList = this.stockService.findStockAvailableAll();
        List<Account> accounts = this.accountService.findAll();

        model.addAttribute("accounts", accounts);
        model.addAttribute("stockList", stockList);
        model.addAttribute("rentalStatus", RentalStatus.values());

        RentalManage rentalManage = this.rentalManageService.findById(Long.valueOf(id));

        if (!model.containsAttribute("rentalManageDto")) {
            RentalManageDto rentalManageDto = new RentalManageDto();

            rentalManageDto.setId(rentalManage.getId());
            rentalManageDto.setEmployeeId(rentalManage.getAccount().getEmployeeId());
            rentalManageDto.setExpectedRentalOn(rentalManage.getExpectedRentalOn());
            rentalManageDto.setExpectedReturnOn(rentalManage.getExpectedReturnOn());
            rentalManageDto.setStatus(rentalManage.getStatus());
            rentalManageDto.setStockId(rentalManage.getStock().getId());

            model.addAttribute("rentalManageDto", rentalManageDto);

        }

        return "rental/edit";
    }

    @PostMapping("/rental/{id}/edit")
    public String update(@PathVariable("id") String id, @Valid @ModelAttribute RentalManageDto rentalManageDto,
            BindingResult result, RedirectAttributes ra, Model model) {
        try {
            // 貸出管理データを持ってくる
            RentalManage rentalManage = this.rentalManageService.findById(Long.valueOf(id));
            String validerror = rentalManageDto.isValidStatus(rentalManage.getStatus());
            String rentalDateError = rentalCheck(rentalManageDto, Long.valueOf(id));

            if (validerror != null) {
                result.addError(new FieldError("rentalManageDto", "status", validerror));
            }
            if (rentalDateError != null) {
                result.addError(new FieldError("rentalManageDto", "expectedRentalOn", rentalDateError));
            }
            if (rentalDateError != null) {
                result.addError(new FieldError("rentalManageDto", "expectedReturnOn", rentalDateError));
            }

            if (result.hasErrors()) {
                throw new Exception("Validation error.");
            }
            // 登録処理
            rentalManageService.update(Long.valueOf(id), rentalManageDto);

            return "redirect:/rental/index";

        } catch (Exception e) {
            log.error(e.getMessage());

            ra.addFlashAttribute("rentalManageDto", rentalManageDto);
            ra.addFlashAttribute("org.springframework.validation.BindingResult.rentalManageDto", result);

            List<Stock> stockList = this.stockService.findStockAvailableAll();
            List<Account> accounts = this.accountService.findAll();

            model.addAttribute("accounts", accounts);
            model.addAttribute("stockList", stockList);
            model.addAttribute("rentalStatus", RentalStatus.values());

            return "rental/edit";
        }
    }

    // 登録
    public String rentalCheck(RentalManageDto rentalManageDto, String StockId) {
        // 在庫管理番号に紐づけされたレコード取得

        List<RentalManage> rentalAvailable = this.rentalManageService.findByStockIdAndStatusIn(StockId);
        for (RentalManage list : rentalAvailable) {

            if (list.getExpectedReturnOn().after(rentalManageDto.getExpectedRentalOn())
                    && list.getExpectedRentalOn().before(rentalManageDto.getExpectedReturnOn())) {
                return "選択した日付には貸出できません";

            }
        }
        return null;

    }

    // 更新
    public String rentalCheck(RentalManageDto rentalManageDto, Long rentalId) {
        List<RentalManage> rentalAvailable = this.rentalManageService
                .findByStockIdAndStatusIn(rentalManageDto.getStockId(), rentalId);
        for (RentalManage list : rentalAvailable) {

            if (list.getExpectedReturnOn().after(rentalManageDto.getExpectedRentalOn())
                    && list.getExpectedRentalOn().before(rentalManageDto.getExpectedReturnOn())) {
                return "選択した日付には貸出できません";
            }
        }
        return null;
    }

}