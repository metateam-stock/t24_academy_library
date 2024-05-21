package jp.co.metateam.library.controller;

import java.util.List;
import java.util.Optional;

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
import jp.co.metateam.library.model.RentalManage;
import jp.co.metateam.library.model.RentalManageDto;
import jp.co.metateam.library.model.Stock;
import jp.co.metateam.library.service.AccountService;
import jp.co.metateam.library.service.RentalManageService;
import jp.co.metateam.library.service.StockService;
import jp.co.metateam.library.values.RentalStatus;
import lombok.extern.log4j.Log4j2;

/**
 * 貸出管理関連クラスß
 */
@Log4j2
@Controller
public class RentalManageController {

    private final AccountService accountService;
    private final RentalManageService rentalManageService; // この時点では定義しただけで、中身は空っぽ
    private final StockService stockService;

    @Autowired
    public RentalManageController( // コンストラクタ
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

        List<Account> accountList = this.accountService.findAll();
        List<Stock> stockList = this.stockService.findStockAvailableAll();

        model.addAttribute("accounts", accountList);
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
            // if (result.hasErrors()) {
            // throw new Exception("Validation error.");
            // }

            String errorMessageInventoryStatus = CheckInventoryStatus(rentalManageDto.getStockId());
            if (errorMessageInventoryStatus != null) {
                result.addError(
                        new FieldError("rentalManageDto", "stockId", errorMessageInventoryStatus));
            }

            // 貸出期間重複チェックの１つ目のメソッドを呼び出す処理
            String errorMessageFirstDuration = firstAvailabilityCheckForLending(rentalManageDto,
                    rentalManageDto.getStockId());

            if (errorMessageFirstDuration != null) { // isPresent()メソッド：値を持っていればtrue
                result.addError(new FieldError("rentalManageDto", "expectedRentalOn", errorMessageFirstDuration));
                result.addError(new FieldError("rentalManageDto", "expectedReturnOn", errorMessageFirstDuration));
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
    public String edit(@PathVariable("id") String id, Model model) {

        List<Account> accountList = this.accountService.findAll();
        List<Stock> stockList = this.stockService.findAll();

        model.addAttribute("accounts", accountList);
        model.addAttribute("stockList", stockList);
        model.addAttribute("rentalStatus", RentalStatus.values());

        if (!model.containsAttribute("rentalManageDto")) {
            // rentalManageを変数としたクラスを作成する
            RentalManage rentalManage = this.rentalManageService.findById(Long.parseLong(id));
            RentalManageDto rentalManageDto = new RentalManageDto();
            rentalManageDto.setId(rentalManage.getId());
            rentalManageDto.setEmployeeId(rentalManage.getAccount().getEmployeeId());
            rentalManageDto.setExpectedRentalOn(rentalManage.getExpectedRentalOn());
            rentalManageDto.setExpectedReturnOn(rentalManage.getExpectedReturnOn());
            rentalManageDto.setStockId(rentalManage.getStock().getId());
            rentalManageDto.setStatus(rentalManage.getStatus());
            model.addAttribute("rentalManageDto", rentalManageDto);
            // DTOという箱に必要なデータを入れて、それをHTML側に送る
        }

        return "rental/edit";
    }

    @PostMapping("/rental/{id}/edit")
    public String update(@PathVariable("id") String id, @Valid @ModelAttribute RentalManageDto rentalManageDto,
            BindingResult result, RedirectAttributes ra) {
        try {
            if (result.hasErrors()) {
                throw new Exception("Validation error.");
            }

            // バリデーションチェック → RentalManageDtoクラスの中に記入していく
            Long longId = Long.parseLong(id);
            RentalManage rentalManage = this.rentalManageService.findById(longId); // rentalManageは35行目辺りで定義済み
            Integer preRentalStatus = rentalManage.getStatus();
            // Integer newRentalStatus = rentalManageDto.getStatus(); ←新しく定義する必要ない
            // 上のrentalManageDtoには、編集時にHTML側から送られてくるデータが入っている

            Optional<String> errorMessage = rentalManageDto.validateStatus(preRentalStatus);

            if (errorMessage.isPresent()) { // isPresent()メソッド：値を持っていればtrue
                result.addError(new FieldError("rentalManageDto", "status", errorMessage.get()));
                throw new Exception(errorMessage.get());
            }

            // 日付妥当性チェック(返却予定日が貸出予定日より前の日付になっていないかどうか)
            Optional<String> errorMessageDate = rentalManageDto.validateDate();

            if (errorMessageDate.isPresent()) { // isPresent()メソッド：値を持っていればtrue
                result.addError(new FieldError("rentalManageDto", "expectedReturnOn", errorMessageDate.get()));
                throw new Exception(errorMessageDate.get());
            }

            // 【追加実装】 日付チェック(0→1の時に貸出予定日が今日に変更されているか) ⇒メソッドはRentalManageDtoクラスにある
            Optional<String> errorMessageExpectedRentalOn = rentalManageDto.validateExpectedRentalOn(preRentalStatus);

            if (errorMessageExpectedRentalOn.isPresent()) { // isPresent()メソッド：値を持っていればtrue
                result.addError(
                        new FieldError("rentalManageDto", "expectedRentalOn", errorMessageExpectedRentalOn.get()));
                throw new Exception(errorMessageExpectedRentalOn.get());// get()以外の選択肢があてはまるか調べてみる
            }

            // 貸出期間重複チェックの2つ目のメソッドを呼び出す処理
            String errorMessageSecondDuration = secondAvailabilityCheckForLending(rentalManageDto,
                    rentalManageDto.getStockId(),
                    rentalManageDto.getId());

            if (errorMessageSecondDuration != null) {
                result.addError(
                        new FieldError("rentalManageDto", "expectedRentalOn", errorMessageSecondDuration));
                result.addError(
                        new FieldError("rentalManageDto", "expectedReturnOn", errorMessageSecondDuration));
                throw new Exception(errorMessageSecondDuration);
            }

            // 更新処理
            this.rentalManageService.update(Long.parseLong(id), rentalManageDto);

            return "redirect:/rental/index";
        } catch (Exception e) {
            log.error(e.getMessage());

            ra.addFlashAttribute("rentalManageDto", rentalManageDto);
            ra.addFlashAttribute("org.springframework.validation.BindingResult.rentalManageDto", result);

            return "redirect:/rental/" + id + "/edit";
        }
    }

    // 在庫ステータスチェック
    private String CheckInventoryStatus(String id) {
        Stock stock = this.stockService.findById(id);
        if (stock.getStatus() == 0) {
            return null; // 利用可→エラーメッセージなし
        } else {
            return "この本はご利用できません"; // 利用不可→エラーメッセージを設定
        }
    }

    // 貸出期間重複チェック「貸出登録の時」⇒貸出管理番号が付与されていない状態(引数が貸出編集の時と異なるため、違うメソッドが必要)
    public String firstAvailabilityCheckForLending(RentalManageDto rentalManageDto, String id) {
        List<RentalManage> rentalManageList = this.rentalManageService.findByStockIdAndStatusIn(id);
        if (rentalManageList != null) {
            // 拡張for文
            for (RentalManage rentalManage : rentalManageList) {
                if (rentalManageDto.getExpectedReturnOn().after(rentalManage.getExpectedRentalOn()) &&
                        rentalManageDto.getExpectedRentalOn().before(rentalManage.getExpectedReturnOn())) {
                    return "貸出期間が重複しているため、この本を借りることができません。日付を変更してください。";
                }
            }
            return null;
        } else {
            return null;
        }
    }

    // 貸出期間重複チェック「貸出編集の時」⇒貸出管理番号が付与されている状態(引数が貸出登録の時と異なるため、違うメソッドが必要)
    public String secondAvailabilityCheckForLending(RentalManageDto rentalManageDto, String id, Long rentalId) {
        List<RentalManage> rentalManageList = this.rentalManageService.findByStockIdAndStatusIn(id, rentalId);
        if (rentalManageList != null) {
            // 拡張for文
            for (RentalManage rentalManage : rentalManageList) {
                if (rentalManageDto.getExpectedReturnOn().after(rentalManage.getExpectedRentalOn()) &&
                        rentalManageDto.getExpectedRentalOn().before(rentalManage.getExpectedReturnOn())) {
                    return "貸出期間が重複しているため、この本を借りることができません。日付を変更してください。";
                }
            }
            return null;
        } else {
            return null;
        }
    }
}
