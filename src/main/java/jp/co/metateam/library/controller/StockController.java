package jp.co.metateam.library.controller;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import jp.co.metateam.library.model.BookMst;
import jp.co.metateam.library.model.BookMstDto;
import jp.co.metateam.library.model.Stock;
import jp.co.metateam.library.model.StockDto;
import jp.co.metateam.library.service.BookMstService;
import jp.co.metateam.library.model.CalendarDto;
import jp.co.metateam.library.service.StockService;
import jp.co.metateam.library.values.StockStatus;
import lombok.extern.log4j.Log4j2;

/**
 * 在庫情報関連クラス
 */
@Log4j2
@Controller
public class StockController {

    private final BookMstService bookMstService;
    private final StockService stockService;

    @Autowired
    public StockController(BookMstService bookMstService, StockService stockService) {
        this.bookMstService = bookMstService;
        this.stockService = stockService;
    }

    @GetMapping("/stock/index")
    public String index(Model model) {
        List <Stock> stockList = this.stockService.findAll();

        model.addAttribute("stockList", stockList);

        return "stock/index";
    }

    @GetMapping("/stock/add")
    public String add(Model model) {
        List<BookMst> bookMstList = this.bookMstService.findAll();

        model.addAttribute("bookMstList", bookMstList);
        model.addAttribute("stockStatus", StockStatus.values());

        if (!model.containsAttribute("stockDto")) {
            model.addAttribute("stockDto", new StockDto());
        }

        return "stock/add";
    }

    @PostMapping("/stock/add")
    public String save(@Valid @ModelAttribute StockDto stockDto, BindingResult result, RedirectAttributes ra) {
        try {
            if (result.hasErrors()) {
                throw new Exception("Validation error.");
            }
            // 登録処理
            this.stockService.save(stockDto);

            return "redirect:/stock/index";
        } catch (Exception e) {
            log.error(e.getMessage());

            ra.addFlashAttribute("stockDto", stockDto);
            ra.addFlashAttribute("org.springframework.validation.BindingResult.stockDto", result);

            return "redirect:/stock/add";
        }
    }

    @GetMapping("/stock/{id}")
    public String detail(@PathVariable("id") String id, Model model) {
        Stock stock = this.stockService.findById(id);

        model.addAttribute("stock", stock);

        return "stock/detail";
    }

    @GetMapping("/stock/{id}/edit")
    public String edit(@PathVariable("id") String id, Model model) {
        List<BookMst> bookMstList = this.bookMstService.findAll();

        model.addAttribute("bookMstList", bookMstList);
        model.addAttribute("stockStatus", StockStatus.values());

        if (!model.containsAttribute("stockDto")) {
            StockDto stockDto = new StockDto();
            Stock stock = this.stockService.findById(id);
            stockDto.setId(stock.getId());
            stockDto.setPrice(stock.getPrice());
            stockDto.setStatus(stock.getStatus());
            stockDto.setBookMst(stock.getBookMst());

            model.addAttribute("stockDto", stockDto);
        }

        return "stock/edit";
    }

    @PostMapping("/stock/{id}/edit")
    public String update(@PathVariable("id") String id, @Valid @ModelAttribute StockDto stockDto, BindingResult result, RedirectAttributes ra) {
        try {
            if (result.hasErrors()) {
                throw new Exception("Validation error.");
            }
            // 登録処理
            stockService.update(id, stockDto);

            return "stock/index";
        } catch (Exception e) {
            log.error(e.getMessage());

            ra.addFlashAttribute("stockDto", stockDto);
            ra.addFlashAttribute("org.springframework.validation.BindingResult.stockDto", result);

            return "redirect:/stock/edit";
        }
    }

    @GetMapping("/stock/calendar")
    //メソッドがパブリックで、ビューにメソッドが文字列を返し、calendarはメソッド名
    public String calendar(@RequestParam(required = false) Integer year, @RequestParam(required = false) Integer month, Model model) {
        //指定された年と月に基づいて LocalDate オブジェクトを作成
        //year と month が null かをチェックし、どちらかが null の場合は現在の日付を使用
        //そうでない場合は、指定された年と月を使用して指定された日付を作成。ただし、月の日付は 1 日となる
        LocalDate today = year == null || month == null ? LocalDate.now() : LocalDate.of(year, month, 1);

        //ターゲットの年を設定、year が null の場合は、今日の年を使用
        //そうでない場合は、指定された年を使用
        Integer targetYear = year == null ? today.getYear() : year;

        //ターゲットの月を設定。today.getMonthValue() を使用して、今日の月を取得
        Integer targetMonth = today.getMonthValue();

        //指定された年と月に基づいて、月の開始日を作成
        //LocalDate.of(year, month, dayOfMonth) を使用して、指定された年と月の最初の日を表す LocalDate オブジェクトを作成
        //targetYear と targetMonth が使用され、月の最初の日が設定される
        LocalDate startDate = LocalDate.of(targetYear, targetMonth, 1);

        //月の日数を計算
        //startDate.lengthOfMonth() を使用して、指定された月の日数を取得
        //lengthOfMonth() メソッドは、指定された月の日数を返します。例）1月の場合は31、2月の場合は28または29（うるう年の場合）
        Integer daysInMonth = startDate.lengthOfMonth();

        //メソッドの呼び出し
        //与えられた年、月、開始日、および月の日数を元に、週の曜日を生成するためのメソッド
        List<Object> daysOfWeek = this.stockService.generateDaysOfWeek(targetYear, targetMonth, startDate, daysInMonth);
        //与えられた年、月、および月の日数を元に、株の値を生成するためのメソッド
        List<List<CalendarDto>>stocks = this.stockService.generateValues(targetYear, targetMonth, daysInMonth);
        //書籍タイトルを取得
        //List<BookMstDto> bookMstList = this.bookMstService.findAvailableWithStockCount();

        model.addAttribute("targetYear", targetYear);
        model.addAttribute("targetMonth", targetMonth);
        model.addAttribute("daysOfWeek", daysOfWeek);
        model.addAttribute("daysInMonth", daysInMonth);

        model.addAttribute("stocks", stocks);



        return "stock/calendar";
    }
}



//書籍タイトルを取得
        //List<BookMstDto> bookMstList = this.bookMstService.findAvailableWithStockCount();
//書籍タイトルをモデルに追加
        //model.addAttribute("bookMstList", bookMstList);