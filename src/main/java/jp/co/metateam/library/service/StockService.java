package jp.co.metateam.library.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import java.util.Calendar;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jp.co.metateam.library.constants.Constants;
import jp.co.metateam.library.model.BookMst;
import jp.co.metateam.library.model.BookMstDto;

import jp.co.metateam.library.model.Stock;
import jp.co.metateam.library.model.StockDto;
import jp.co.metateam.library.model.StockListDto;
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
    public List<Stock> findLendableBook(Date rentalDay, Long id) {
        return this.stockRepository.findLendableBook(rentalDay, id);
    }

    @Transactional
    public List<BookMst> findByBookTitle(String title) {
        return this.bookMstRepository.findByBookTitle(title);
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

    public List<BookMstDto> findbyBookIdList(List<BookMstDto> bookMsts, List<String> stock_id,
            Integer year, Integer month, Integer daysInMonth) {

        List<Stock> stockAvailable = this.stockRepository.findbyBookIdList();
        List<BookMstDto> stockOfNumber = new ArrayList<>();

        int targetYear = year == null ? LocalDate.now().getYear() : year;
        int targetMonth = month == null ? LocalDate.now().getMonthValue() : month;

        for (BookMstDto bookMst : bookMsts) {
            long bookId = bookMst.getId(); // 書籍IDを取得
            int stockCount = 0;
            List<String> stockIds = new ArrayList<>();
            // 在庫数を計算する
            for (Stock stock : stockAvailable) {
                if (stock.getBookMst().getId() == bookId) {
                    stockCount++;
                    stockIds.add(stock.getId());
                }
            }

            // BookMstDtoに在庫数を設定する
            bookMst.setStockCount(stockCount);
            List<StockListDto> values = new ArrayList<StockListDto>();
            // 現在日付を取得
            LocalDate today = LocalDate.now();

            // 日付分
            for (int dayOfMonth = 1; dayOfMonth <= daysInMonth; dayOfMonth++) {
                StockListDto stockListDto = new StockListDto();

                // 日付を作成
                LocalDate currentDateOfMonth = LocalDate.of(targetYear, targetMonth, dayOfMonth);
                stockListDto.setSelectedDay(currentDateOfMonth);
                // 過去日の判定
                if (today != null && currentDateOfMonth.isBefore(today)) {
                    stockListDto.setStockCount("×");
                    values.add(stockListDto);
                    continue; // 次の日付へ
                }

                // カレンダーメソッド
                Calendar calendar = Calendar.getInstance();
                calendar.clear();
                calendar.set(targetYear, targetMonth - 1, dayOfMonth);

                // 貸出待ちと貸出中の数を取得する
                Long stockDailyWaiting = this.rentalManageRepository.findByDailySchedule(calendar.getTime(),
                        stockIds);

                // 在庫数から貸出待ちと貸出中を引き算してセットする→被っていない在庫数
                Long availableCount = stockCount - (stockDailyWaiting != null ? stockDailyWaiting : 0);

                // 計算してavailableCountに入れたデータを String型のtotalValueに変換
                String totalValue = (availableCount <= 0) ? "×" : Long.toString(availableCount);

                // 取得した日付ごとの利用可能在庫数を入れる
                stockListDto.setStockCount(totalValue);
                values.add(stockListDto);
                // 日付ごとの在庫数をセットする

            }
            bookMst.setStockList(values);

            // 最終的な結果リストに追加する
            stockOfNumber.add(bookMst);
        }
        return stockOfNumber;
    }

    public List<Stock> getStockId(Date selectedDay, Long id) {

        List<Stock> lendableBooks = this.stockRepository.findLendableBook(selectedDay, id);

        return lendableBooks;
    }

}