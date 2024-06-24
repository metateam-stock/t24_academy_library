package jp.co.metateam.library.model;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CalendarDto {

    private String title;

    // 貸出管理テーブルの在庫管理番号
    private String stockId;

    // 利用可能在庫数
    private Long totalBookCounts;

    private Date expectedRentalOn;

    // 日付ごとの在庫数
    private Object countOfDaysBook;

    // 日ごとの在庫数が「０」の時に「×」を表示させるための変数 ⇐ 使わない
    // private String bookCount0;
}
