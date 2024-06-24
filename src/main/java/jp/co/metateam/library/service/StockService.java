package jp.co.metateam.library.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Calendar;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestParam;

import jp.co.metateam.library.constants.Constants;
import jp.co.metateam.library.model.BookMst;
import jp.co.metateam.library.model.CalendarDto;
import jp.co.metateam.library.model.RentalManage;
import jp.co.metateam.library.model.Stock;
import jp.co.metateam.library.model.StockDto;
import jp.co.metateam.library.repository.BookMstRepository;
import jp.co.metateam.library.repository.RentalManageRepository;
import jp.co.metateam.library.repository.StockRepository;

@Service
public class StockService {
    private final BookMstRepository bookMstRepository;
    private final StockRepository stockRepository;
    private final RentalManageRepository rentalManageRepository;

    @Autowired
    public StockService(BookMstRepository bookMstRepository, StockRepository stockRepository,
            RentalManageRepository rentalManageRepository) {
        this.bookMstRepository = bookMstRepository;
        this.stockRepository = stockRepository;
        this.rentalManageRepository = rentalManageRepository;
    }

    @Transactional
    public List<Stock> findAll() {
        List<Stock> stocks = this.stockRepository.findByDeletedAtIsNull();

        return stocks;
    }

    @Transactional
    public List<Stock> findStockAvailableAll() {
        List<Stock> stocks = this.stockRepository.findByDeletedAtIsNullAndStatus(Constants.STOCK_AVAILABLE);

        return stocks;
    }

    @Transactional
    public Stock findById(String id) {
        return this.stockRepository.findById(id).orElse(null);
    }

    @Transactional
    public List<Object[]> findTitleAndCountId() {
        return this.stockRepository.findTitleAndCountId();
    }

    @Transactional
    public Long findByTitleCount(String title, Date date) {
        return this.rentalManageRepository.findByTitleCount(title, date);
    }

    @Transactional
    public List<String> findByStockId(String title, Date date) {
        return this.rentalManageRepository.findByStockId(title, date);
    }

    @Transactional
    public void save(StockDto stockDto) throws Exception {
        try {
            Stock stock = new Stock();
            BookMst bookMst = this.bookMstRepository.findById(stockDto.getBookId()).orElse(null);
            if (bookMst == null) {
                throw new Exception("BookMst record not found.");
            }

            stock.setBookMst(bookMst);
            stock.setId(stockDto.getId());
            stock.setStatus(stockDto.getStatus());
            stock.setPrice(stockDto.getPrice());

            // データベースへの保存
            this.stockRepository.save(stock);
        } catch (Exception e) {
            throw e;
        }
    }

    @Transactional
    public void update(String id, StockDto stockDto) throws Exception {
        try {
            Stock stock = findById(id);
            if (stock == null) {
                throw new Exception("Stock record not found.");
            }

            BookMst bookMst = stock.getBookMst();
            if (bookMst == null) {
                throw new Exception("BookMst record not found.");
            }

            stock.setId(stockDto.getId());
            stock.setBookMst(bookMst);
            stock.setStatus(stockDto.getStatus());
            stock.setPrice(stockDto.getPrice());

            // データベースへの保存
            this.stockRepository.save(stock);
        } catch (Exception e) {
            throw e;
        }
    }

    public List<Object> generateDaysOfWeek(int year, int month, LocalDate startDate, int daysInMonth) {
        List<Object> daysOfWeek = new ArrayList<>();
        for (int dayOfMonth = 1; dayOfMonth <= daysInMonth; dayOfMonth++) {
            LocalDate date = LocalDate.of(year, month, dayOfMonth);
            DateTimeFormatter formmater = DateTimeFormatter.ofPattern("dd(E)", Locale.JAPANESE);
            daysOfWeek.add(date.format(formmater));
        }

        return daysOfWeek;
    }

    // ある日にちの在庫数(1セル分の数字1つ)を取得するメソッド
    // public List<String> generateValues(Integer year, Integer month, Integer
    // daysInMonth) {
    // // FIXME ここで各書籍毎の日々の在庫を生成する処理を実装する
    // // FIXME ランダムに値を返却するサンプルを実装している
    // // String[] stockNum = { "1", "2", "3", "4", "×" };
    // // Random rnd = new Random();
    // // List<String> values = new ArrayList<>();
    // // values.add("スッキリわかるJava入門 第4版"); // 対象の書籍名
    // // values.add("10"); // 対象書籍の在庫総数

    // for (int i = 1; i <= daysInMonth; i++) {
    // int index = rnd.nextInt(stockNum.length);
    // values.add(stockNum[index]);
    // }
    // return values;
    // }

    public List<List<CalendarDto>> generateValues(Integer year, Integer month, Integer daysInMonth) {

        List<Object[]> titleAndCountId = findTitleAndCountId();
        Calendar calendar = Calendar.getInstance();
        List<List<CalendarDto>> calendarList = new ArrayList<>();
        String noCountOfDaysBook = "×";

        for (Object[] array : titleAndCountId) {
            List<CalendarDto> listOfDays = new ArrayList<>();
            String title = (String) array[0];
            Long totalBookCounts = (Long) array[1];

            // 日付ごとの在庫数を格納するリスト
            for (int i = 1; i <= daysInMonth; i++) {
                CalendarDto calendarDto = new CalendarDto();
                calendarDto.setTitle(title);
                calendarDto.setTotalBookCounts(totalBookCounts);

                // 日付の設定
                calendar.set(year, month - 1, i, 0, 0, 0);
                Date date = calendar.getTime();
                calendarDto.setExpectedRentalOn(date);

                Long findByTitleCount = findByTitleCount(title, date);
                List<String> findByStockId = findByStockId(title, date);

                // 在庫数の計算と設定
                Long countOfDaysBook = totalBookCounts - findByTitleCount;
                if (countOfDaysBook == 0) {
                    calendarDto.setCountOfDaysBook(noCountOfDaysBook);
                } else {
                    calendarDto.setCountOfDaysBook(countOfDaysBook);
                }

                // StockId の設定
                if (!findByStockId.isEmpty()) {
                    calendarDto.setStockId(findByStockId.get(0));
                }

                listOfDays.add(calendarDto);
            }
            calendarList.add(listOfDays);
        }

        return calendarList;
    }
}
