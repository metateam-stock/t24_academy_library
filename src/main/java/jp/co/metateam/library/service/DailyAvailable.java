package jp.co.metateam.library.service;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class DailyAvailable {
    private Integer lendable_book;
    private String stockId;
    private Date expectedRentalOn;

}
