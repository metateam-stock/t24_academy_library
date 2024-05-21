package jp.co.metateam.library.model;

import java.sql.Timestamp;
import java.util.Date;
import java.time.*;
import java.util.Optional;

import org.hibernate.type.descriptor.java.LocalDateJavaType;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jp.co.metateam.library.values.RentalStatus;
import lombok.Getter;
import lombok.Setter;

/**
 * 貸出管理DTO
 */
@Getter
@Setter
public class RentalManageDto {

    private Long id;

    @NotEmpty(message = "在庫管理番号は必須です")
    private String stockId;

    @NotEmpty(message = "社員番号は必須です")
    private String employeeId;

    @NotNull(message = "貸出ステータスは必須です")
    private Integer status;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "貸出予定日は必須です")
    private Date expectedRentalOn;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "返却予定日は必須です")
    private Date expectedReturnOn;

    private Timestamp rentaledAt;

    private Timestamp returnedAt;

    private Timestamp canceledAt;

    private Stock stock;

    private Account account;

    // バリデーションチェック用のメソッドを記載していく→RentalManageControllerクラスから呼び出される
    public Optional<String> validateStatus(Integer preRentalStatus) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        if (preRentalStatus == RentalStatus.RENT_WAIT.getValue() && preRentalStatus != this.status) { // 0から変更してるかどうか
            if (this.status == RentalStatus.RETURNED.getValue()) { // 2に変更してるかどうか
                return Optional.of("「貸出待ち」から「返却済み」には変更できません"); // エラーメッセージの設定
            }
        } else if (preRentalStatus == RentalStatus.RENTAlING.getValue() && preRentalStatus != this.status) {// 1から変更してるかどうか
            if (this.status != RentalStatus.RETURNED.getValue()) {// 変更先が2以外かどうか
                return Optional.of("「貸出中」から「貸出待ち」または「キャンセル」には変更できません");// エラーメッセージの設定
            }

        } else if ((preRentalStatus == RentalStatus.RETURNED.getValue() && preRentalStatus != this.status) ||
                (preRentalStatus == RentalStatus.CANCELED.getValue() && preRentalStatus != this.status)) {// 2または3から変更してるかどうか
            return Optional.of("「返却済み」または「キャンセル」から貸出ステータスの変更はできません");// エラーメッセージの設定
            // 確認事項：詳細設計において、上のreturn文に該当する「エラーメッセージの設定」がない(詳細設計のミス？)
        }

        return Optional.empty();

    }

    public Optional<String> validateDate() {
        if (expectedReturnOn.before(expectedRentalOn)) {
            return Optional.of("「返却予定日」を「貸出予定日」より前の日付にすることはできません");
        }
        return Optional.empty();
    }

    // 【追加実装】
    public Optional<String> validateExpectedRentalOn(Integer preRentalStatus) {
        LocalDate date = LocalDate.now();

        // Date型クラス(expectedRentalOn)⇒LocalDate型クラスに変換する
        LocalDate localDate = expectedRentalOn.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        // 0から変更してるかどうか
        if (preRentalStatus == RentalStatus.RENT_WAIT.getValue() && preRentalStatus != this.status) {
            // 1に変更してるかどうか
            if (this.status == RentalStatus.RENTAlING.getValue()) {
                // 編集後の日付が、今日の日付じゃなかったらtrue
                if (!(localDate.equals(date))) {
                    return Optional.of("本日の日付に変更してください");
                }
            }
        }
        return Optional.empty();
    }
}
