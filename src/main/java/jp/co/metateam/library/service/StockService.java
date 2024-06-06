package jp.co.metateam.library.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jp.co.metateam.library.constants.Constants;
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

    public List<Object> generateDaysOfWeek(int year, int month, LocalDate startDate, int daysInMonth) {
        List<Object> daysOfWeek = new ArrayList<>();
        for (int dayOfMonth = 1; dayOfMonth <= daysInMonth; dayOfMonth++) {
            LocalDate date = LocalDate.of(year, month, dayOfMonth);
            DateTimeFormatter formmater = DateTimeFormatter.ofPattern("dd(E)", Locale.JAPANESE);
            daysOfWeek.add(date.format(formmater));
        }

        return daysOfWeek;
    }

    @Transactional
    public List<List<CalendarDto>> generateValues(Integer year, Integer month, Integer daysInMonth) {

        List<List<CalendarDto>> bigValues = new ArrayList<>();
        List<Object[]> totalStockPerTitle = this.stockRepository.countByTotalStockPerTitle();

        for (int i = 0; i < totalStockPerTitle.size(); i++) {
            List<CalendarDto> values = new ArrayList<>();
            Object[] bookInfo = totalStockPerTitle.get(i);
            String title = (String) bookInfo[0];
            Long stockCount = (Long) bookInfo[1];

            // for分 タイトル別、日ごとの貸出可能残数
            for (int dayOfMonth = 1; dayOfMonth <= daysInMonth; dayOfMonth++) {
                CalendarDto calendarDto = new CalendarDto();
                calendarDto.setTitle(title);
                calendarDto.setStockCount(stockCount);

                LocalDate currentDate = LocalDate.of(year, month, dayOfMonth);
                Date specifiedDate = Date.from(currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                calendarDto.setExpectedRentalOn(specifiedDate);

                // 在庫管理番号取得
                List<String> stockIds = this.rentalManageRepository.selectByStockId(title, specifiedDate);
                calendarDto.setStockId(stockIds.get(0));

                Long rentalCount = this.rentalManageRepository.countBySpecifiedDateRentals(title, specifiedDate);
                // 在庫数ー貸出数
                Long countAvailableRental = stockCount - rentalCount;
                calendarDto.setCountAvailableRental(countAvailableRental);

                values.add(calendarDto);
            }

            bigValues.add(values);// 少リストを大リストに追加
        }

        return bigValues;
    }
}
