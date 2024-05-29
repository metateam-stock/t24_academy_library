package jp.co.metateam.library.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jp.co.metateam.library.service.AccountService;
import jp.co.metateam.library.service.RentalManageService;
import jp.co.metateam.library.service.StockService;
import jp.co.metateam.library.constants.Constants;
import java.util.Date;
import lombok.extern.log4j.Log4j2;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jp.co.metateam.library.model.RentalManage;
import jp.co.metateam.library.model.StockDto;
import jp.co.metateam.library.repository.AccountRepository;

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
    private RentalStatus rentalStatus;

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
        return "/rental/index";
    }

    @GetMapping("/rental/add")
    public String add(Model model) {

        // 在庫が利用可能な在庫テーブルからデータを取得
        List<Stock> stockList = this.stockService.findStockAvailableAll();
        // アカウントテーブルから全件取得
        List<Account> accountList = this.accountService.findAll();

        // 在庫管理番号のデータをmodelに追加
        model.addAttribute("stockList", stockList);
        // 貸出ステータスのデータをmodelに追加
        model.addAttribute("rentalStatus", RentalStatus.values());
        // 社員番号のデータをmodelに追加
        model.addAttribute("accounts", accountList);
        // rentalMnageDtoに存在しなかったら、rentalMnageDtoに新しく登録？
        if (!model.containsAttribute("rentalManageDto")) {
            model.addAttribute("rentalManageDto", new RentalManageDto());
        }

        return "rental/add";
    }

    @PostMapping("/rental/add")
    public String save(@Valid @ModelAttribute RentalManageDto rentalManageDto, BindingResult result,
            RedirectAttributes ra, Model model) {
        try {
            String dateError = rentalManageDto.dateRentalReturnError();
            String statusError = rentalManageDto.rentalAddStatusError();
            String rentalOnDateError = findAvailableWithRentalDate(rentalManageDto, rentalManageDto.getStockId());
            String returnOnDateError = findAvailableWithRentalDate(rentalManageDto, rentalManageDto.getStockId());

            if (dateError != null) {
                result.addError(new FieldError("rentalManageDto", "expectedRentalOn", dateError));
            }

            if (statusError != null) {
                result.addError(new FieldError("rentalManageDto", "status", statusError));
            }

            if (rentalOnDateError != null) {
                result.addError(new FieldError("rentalManageDto", "expectedRentalOn", rentalOnDateError));
            }

            if (returnOnDateError != null) {
                result.addError(new FieldError("rentalManageDto", "expectedReturnOn", returnOnDateError));
            }

            if (result.hasErrors()) {
                throw new Exception("Validation error.");
            }
            // 登録処理
            this.rentalManageService.save(rentalManageDto, result, rentalManageDto.getStockId());

            return "redirect:/rental/index";
        } catch (Exception e) {
            log.error(e.getMessage());

            ra.addFlashAttribute("rentalManageDto", rentalManageDto);
            ra.addFlashAttribute("org.springframework.validation.BindingResult.stockDto", result);

            List<Account> accountList = this.accountService.findAll();
            List<Stock> stockList = this.stockService.findStockAvailableAll();

            model.addAttribute("stockList", stockList);
            model.addAttribute("rentalStatus", RentalStatus.values());
            model.addAttribute("accounts", accountList);

            return "rental/add";
        }
    }

    // 貸出編集画面のHTMLとControllerの画面をつなぐGETメソッドコード
    @GetMapping("/rental/{id}/edit")
    public String edit(@PathVariable("id") String id, Model model) {
        List<Account> accountList = this.accountService.findAll();
        List<Stock> stockList = this.stockService.findStockAvailableAll();

        model.addAttribute("stockList", stockList);
        model.addAttribute("rentalStatus", RentalStatus.values());
        model.addAttribute("accounts", accountList);

        RentalManage rentalManage = this.rentalManageService.findById(Long.valueOf(id));

        if (!model.containsAttribute("rentalManageDto")) {
            RentalManageDto rentalManageDto = new RentalManageDto();

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
    // BindingResult resultにRentalManageDtoでの入力チェックを行った情報を入れている
    public String update(@PathVariable("id") String id, @Valid @ModelAttribute RentalManageDto rentalManageDto,
            BindingResult result, RedirectAttributes ra, Model model, AccountRepository accountRepository) {
        try {
            // 貸出管理番号に紐づく貸出情報を取得
            RentalManage rentalManage = this.rentalManageService.findById(Long.valueOf(id));
            // 貸出予定日<返却予定日のエラーチェック
            String dateError = rentalManageDto.dateRentalReturnError();
            // ステータスチェックを行うメソッドを呼びだす。そしてその結果(エラー文or null)をrentalStatusErrorに入れている
            String rentalStatusError = rentalManageDto.isStatusError(rentalManage.getStatus());
            // 可否チェック
            String returnOnDateError = findAvailableWithRentalDate(rentalManageDto, Long.valueOf(id));
            String rentalOnDateError = findAvailableWithRentalDate(rentalManageDto, Long.valueOf(id));

            // 貸出予定日<返却予定日のエラーチェックにエラーがあればresultに追加
            if (dateError != null) {
                result.addError(new FieldError("rentalManageDto", "expectedRentalOn", dateError));                
            }

            // ステータスチェックの結果(エラー文or null)をresultに追加
            if (rentalStatusError != null) {
                result.addError(new FieldError("rentalManageDto", "status", rentalStatusError));
            }

            // 可否チェックの結果(エラー文or null)をresultに追加
            if (rentalOnDateError != null) {
                result.addError(new FieldError("rentalManageDto", "expectedRentalOn", rentalOnDateError));
            }

            // 可否チェックの結果(エラー文or null)をresultに追加
            if (returnOnDateError != null) {
                result.addError(new FieldError("rentalManageDto", "expectedReturnOn", returnOnDateError));
            }

            // 「resultにエラーがある」がtrueだったらcathch内処理が実行
            if (result.hasErrors()) {
                throw new Exception("Validation error.");
            }

            // 登録処理
            rentalManageService.update(Long.valueOf(id), rentalManageDto, result, rentalManageDto.getStockId());

            return "redirect:/rental/index";
        } catch (Exception e) {
            log.error(e.getMessage());

            ra.addFlashAttribute("rentalManageDto", rentalManageDto);
            ra.addFlashAttribute("org.springframework.validation.BindingResult.stockDto", result);

            List<Account> accountList = this.accountService.findAll();
            List<Stock> stockList = this.stockService.findStockAvailableAll();

            model.addAttribute("stockList", stockList);
            model.addAttribute("rentalStatus", RentalStatus.values());
            model.addAttribute("accounts", accountList);

            return "/rental/edit";
        }
    }

    // 貸出登録の可否チェックメソッド
    @Query
    public String findAvailableWithRentalDate(RentalManageDto rentalManageDto, String stockId) {
        List<RentalManage> rentalAvailable = this.rentalManageService.findByStockIdAndStatusIn(stockId);

        for (RentalManage exist : rentalAvailable) {
            // リスト内の貸出日<=Dtoの返却日 かつ Dtoの貸出日<=リスト内の返却日
            // だった時にエラー表示
            if (exist.getExpectedRentalOn().compareTo(rentalManageDto.getExpectedReturnOn()) <= 0) {
                if (rentalManageDto.getExpectedRentalOn().compareTo(exist.getExpectedReturnOn()) <= 0) {
                    return "選択された日付は登録済みの貸出情報と重複しています";
                }
            }        
        }return null;
    }

    // 貸出編集の可否チェックメソッド
    public String findAvailableWithRentalDate(RentalManageDto rentalManageDto, Long rentalId) {
        List<RentalManage> rentalAvailable = this.rentalManageService
                .findByStockIdAndStatusIn(rentalManageDto.getStockId(), rentalId);
        for (RentalManage exist : rentalAvailable) {
            if (exist.getExpectedRentalOn().compareTo(rentalManageDto.getExpectedReturnOn()) <= 0) {
                if (rentalManageDto.getExpectedRentalOn().compareTo(exist.getExpectedReturnOn()) <= 0) {
                    return "選択された日付は登録済みの貸出情報と重複しています";
                }
            }
        }
        return null;
    }
}
