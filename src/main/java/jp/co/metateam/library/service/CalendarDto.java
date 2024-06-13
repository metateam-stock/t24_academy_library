package jp.co.metateam.library.service;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class CalendarDto {
  private String title;
  private int count;
  public List<DailyAvailable> dailyDetail;

}
