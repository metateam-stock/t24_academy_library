package jp.co.metateam.library.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Date;

import org.hibernate.sql.ast.tree.insert.Values;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jp.co.metateam.library.constants.Constants;
import jp.co.metateam.library.controller.StockController;
import jp.co.metateam.library.model.BookMst;
import jp.co.metateam.library.model.CalendarDto;
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
    private String bookTitle;
    private Object rentalStatus;

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

    // 指定された年、月、開始日、および月の日数を受け取り、週の日付を生成
    public List<Object> generateDaysOfWeek(int year, int month, LocalDate startDate, int daysInMonth) {
        // 日付を格納するための新しいリストを作成
        List<Object> daysOfWeek = new ArrayList<>();
        // 1から月の日数までのループ
        for (int dayOfMonth = 1; dayOfMonth <= daysInMonth; dayOfMonth++) {
            // 現在の年、月、およびループのカウンターである日を使用して、LocalDateオブジェクトを生成
            LocalDate date = LocalDate.of(year, month, dayOfMonth);
            // 日付をフォーマットするためのDateTimeFormatterオブジェクトを作成
            DateTimeFormatter formmater = DateTimeFormatter.ofPattern("dd(E)", Locale.JAPANESE);
            daysOfWeek.add(date.format(formmater));
        }

        return daysOfWeek;
    }

    public List<List<CalendarDto>> generateValues(Integer year, Integer month, Integer daysInMonth) {

        // リスト(大きな箱)作成
        List<List<CalendarDto>> bigValues = new ArrayList<>();
        // 書籍名と利用可能在庫数を取得
        List<Object[]> stockCount = this.stockRepository.findByBookMstIdAndStatus();

        for (Object[] bookIn : stockCount) {
            // 書籍タイトルを取得
            String bookTitle = (String) bookIn[0];
            // 在庫数を取得
            Long stockCountValue = (Long) bookIn[1];
            // 取得した書籍名と在庫数をリストに追加
            List<CalendarDto> abkList = new ArrayList<>();


            // 日付ループ
            // 1日から月の最終日までの日数をループ
            for (int daysLastMonth = 1; daysLastMonth <= daysInMonth; daysLastMonth++) {
                // オブジェクト作成
                LocalDate date = LocalDate.of(year, month, daysLastMonth);
                // 12時から日付が変わるようDate型に変換（時間設定）
                Date atDate = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
                // 利用可能なStockIdを取得
                Long rentalCount = this.rentalManageRepository.countByborrowingbook(bookTitle, atDate);
                // 貸出待ちまたは貸出中のステータスを取得（詳細設計変更箇所）

                // 利用可能在庫数から貸出数を引く
                Long availableCount = stockCountValue - rentalCount;

                CalendarDto calendarDto = new CalendarDto();
                calendarDto.setStockCountValue(stockCountValue);
                calendarDto.setRentalCount(rentalCount);
                calendarDto.setTitle(bookTitle);
                calendarDto.setExpectedRentalOn(atDate);

                // 利用可能在庫数が０の場合「×」
                if (availableCount <= 0) {
                    calendarDto.setAvailableStock("×");
                } else {
                    calendarDto.setAvailableStock(String.valueOf(availableCount));
                }
                
                // add
                abkList.add(calendarDto);
                }
                bigValues.add(abkList);
                }
                return bigValues;
            }
}    
    


// FIXME ここで各書籍毎の日々の在庫を生成する処理を実装する
// FIXME ランダムに値を返却するサンプルを実装している
// String[] stockNum = {"1", "2", "3", "4", "×"};
// Random rnd = new Random();
// List<String> values = new ArrayList<>();
// values.add("スッキリわかるJava入門 第4版"); // 対象の書籍名
// values.add("10"); // 対象書籍の在庫総数

// for (int dayOfMonth = 1; dayOfMonth <= daysInMonth; dayOfMonth++) {
// int index = rnd.nextInt(stockNum.length);
// values.add(stockNum[index]);
// }
// return values;
// }

/*
 * String title = lendableTitles.get(i);
 * //リストから、現在のループで処理するタイトルに対応する情報を取得
 * //タイトルごとの貸し出し可能な本の数の情報を含む配列
 * Object[] bookInfo = countBylendableBooks.get(i);
 * //配列から、貸し出し可能な本の数を取得
 * Long count = (Long) bookInfo[1];
 * //リストにタイトルを追加
 * values.add(title);
 * //貸し出し可能な本の数を文字列に変換し、valuesリストに追加
 * values.add(count.toString());
 */
// データベースからデータを取得
// List<String> lendableTitles = this.stockRepository.findByLendableTitle();
// List<Object[]> countBylendableBooks =
// this.stockRepository.countByLendableBook();