package jp.co.metateam.library.model;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

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

    // 貸出登録可否チェック
    public boolean validRentalDateAdd(List<RentalManage> rentalManageList) {
        for (RentalManage rentalManage : rentalManageList) {
            Date listRentalRentalOn = rentalManage.getExpectedRentalOn();
            Date listRentalReturnOn = rentalManage.getExpectedReturnOn();

            if (!((expectedReturnOn.before(listRentalRentalOn))
                    || (listRentalReturnOn.before(expectedRentalOn)))) {
                return false;
            }
        }
        return true;
    }

    // 貸出編集可否チェック
    public boolean validRentalDateEdit(List<RentalManage> rentalManageList) {
        for (RentalManage rentalManage : rentalManageList) {
            if (!id.equals(rentalManage.getId())) {
                Date listRentalRentalOn = rentalManage.getExpectedRentalOn();
                Date listRentalReturnOn = rentalManage.getExpectedReturnOn();

                if (!((expectedReturnOn.before(listRentalRentalOn))
                        || (listRentalReturnOn.before(expectedRentalOn)))) {
                    return false;
                }
            }
        }
        return true;
    }

    // 貸出ステータスチェック
    public String validStatus(RentalManage valRentalManage) {

        int status = valRentalManage.getStatus();
        int newStatus = this.getStatus();
        LocalDateTime ldt = LocalDateTime.now();
        Date expectedRentalOn = this.getExpectedRentalOn();
        Date expectedReturnOn = this.getExpectedReturnOn();

        LocalDateTime expectedRentalOnLdt = Instant.ofEpochMilli(expectedRentalOn.getTime())
                .atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime expectedReturnOnLdt = Instant.ofEpochMilli(expectedReturnOn.getTime())
                .atZone(ZoneId.systemDefault()).toLocalDateTime();

        if (newStatus == 1 && ldt.isBefore(expectedRentalOnLdt)) {
            return "貸出予定日が未来のためこのステータスは選択できません";

        } else if (newStatus == 2 && ldt.isBefore(expectedReturnOnLdt)) {
            return "返却予定日が未来のためこのステータスは選択できません";

        } else if (status == 0 && newStatus == 2) {
            return "このステータスは選択できません";

        } else if (status == 1 && (newStatus == 0 || newStatus == 3)) {
            return "このステータスは選択できません";

        } else if (status == 2 && (newStatus == 0 || newStatus == 1 || newStatus == 3)) {
            return "返却済みからステータスの変更はできません";

        } else if (status == 3 && (newStatus == 0 || newStatus == 1 || newStatus == 2)) {
            return "キャンセルからステータスの変更はできません";

        }
        return "";
    }

}
