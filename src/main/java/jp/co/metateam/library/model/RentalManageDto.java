package jp.co.metateam.library.model;

import jp.co.metateam.library.values.RentalStatus;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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

    public Optional<String> isStatusError(Integer preStatus) {
        if (preStatus == RentalStatus.RENT_WAIT.getValue() && this.status == RentalStatus.RETURNED.getValue()) {
            return Optional.of("「貸出待ち」から「返却済み」は選択できません");
        } else if (preStatus == RentalStatus.RENTALING.getValue() && this.status == RentalStatus.RENT_WAIT.getValue()) {
            return Optional.of("「貸出中」から「貸出待ち」には変更できません");
        } else if (preStatus == RentalStatus.RENTALING.getValue() && this.status == RentalStatus.CANCELED.getValue()) {
            return Optional.of("「貸出中」から「キャンセル」には変更できません");
        } else if (preStatus == RentalStatus.RETURNED.getValue() && this.status != RentalStatus.RETURNED.getValue()) {
            return Optional.of("「返却済み」から変更はできません");
        } else if (preStatus == RentalStatus.CANCELED.getValue() && this.status != RentalStatus.CANCELED.getValue()) {
            return Optional.of("「キャンセル」から変更はできません");
        }
        return Optional.empty();

    }

    // 日付チェック
    public String isDateError(RentalManage rentalManage, RentalManageDto rentalManageDto) {
        // 現在の日時
        LocalDate nowDate = LocalDate.now(ZoneId.of("Asia/Tokyo"));

        // status 古いのと新しいの
        Integer prestatus = rentalManage.getStatus();
        Integer poststatus = rentalManageDto.getStatus();

        // expectedを二つ
        LocalDate expectedRentalOn = rentalManageDto.getExpectedRentalOn().toInstant().atZone(ZoneId.systemDefault())
                .toLocalDate();
        LocalDate expectedReturnOn = rentalManageDto.getExpectedReturnOn().toInstant().atZone(ZoneId.systemDefault())
                .toLocalDate();

        // if文・貸出待ち→貸出中、・貸出中→返却済み
        if (prestatus == 0 && poststatus == 1) {
            if (!expectedRentalOn.equals(nowDate)) {
                return "貸出予定日は現在の日付を選択してください。";
            }
        }

        if (prestatus == 1 && poststatus == 2) {
            if (!expectedReturnOn.equals(nowDate)) {
                return "返却予定日は現在の日付を選択してください。";
            }
        }
        return null;
    }

    // 貸出予定日＜返却予定日
    public String orderRentalDate(RentalManageDto rentalManageDto) {
        Date expectedRentalOn = rentalManageDto.getExpectedRentalOn();
        Date expectedReturnOn = rentalManageDto.getExpectedReturnOn();

        if (!expectedRentalOn.before(expectedReturnOn)) {
            return "返却予定日は貸出予定日より後の日付を入力してください.";
        }
        return null;
    }

    // 日付Format
    public String validDateFormat(RentalManageDto rentalManageDto) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");

        String expectedRentalOnStr = sdf.format(rentalManageDto.getExpectedRentalOn());
        String expectedReturnOnStr = sdf.format(rentalManageDto.getExpectedReturnOn());

        if (!expectedRentalOnStr.matches("\\d{4}/\\d{2}/\\d{2}")
                || !expectedReturnOnStr.matches("\\d{4}/\\d{2}/\\d{2}")) {
            return "yyyy/mm/ddのフォーマットで入力してください";
        }
        return null;
    }

}
