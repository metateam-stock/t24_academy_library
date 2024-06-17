package jp.co.metateam.library.model;

import java.util.Date;
import java.util.List;

import jp.co.metateam.library.model.Stock;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter

public class CalendarDto {
    private String title;

    private String stockId;

    private int count;
    
    private String availableStock;
    
    private Long rentalCount;
    
    private Long stockCountValue;

    private Long stockCount;
        
    public Date expectedRentalOn;
}
