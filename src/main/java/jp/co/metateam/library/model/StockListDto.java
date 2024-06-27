package jp.co.metateam.library.model;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StockListDto {

    private LocalDate selectedDay;

    private String stockCount;

    
}
