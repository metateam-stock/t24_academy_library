package jp.co.metateam.library.model;

import java.sql.Timestamp;
import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jp.co.metateam.library.values.RentalStatus;
import lombok.Getter;
import lombok.Setter;
import java.util.Optional;
import jp.co.metateam.library.values.RentalStatus;

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

    // 貸出可否チェック
    public Optional<String> isStatusError(Integer preStatus) {
        if (preStatus == RentalStatus.RENT_WAIT.getValue() && this.status == RentalStatus.RETURNED.getValue()) {
            return Optional.of("貸出待ちから返却済みは選択できません");
        } else if (preStatus == RentalStatus.RENTALING.getValue() && this.status == RentalStatus.RENT_WAIT.getValue()) {
            return Optional.of("貸出中から貸出待ちには変更できません");
        } else if (preStatus == RentalStatus.RENTALING.getValue() && this.status == RentalStatus.CANCELED.getValue()) {
            return Optional.of("貸出中からキャンセルには変更できません");
        } else if (preStatus == RentalStatus.RETURNED.getValue() && this.status != RentalStatus.RETURNED.getValue()) {
            return Optional.of("返却済みから変更できません");
        } else if (preStatus == RentalStatus.CANCELED.getValue() && this.status != RentalStatus.CANCELED.getValue()) {
            return Optional.of("キャンセルから変更できません");
        }
        return Optional.empty();
    }
}

    // public Optional<String> checkcalender(Integer oldStatus, Integer status, Date expectedReturnOn) {


    //         Calendar cal = Calendar.getInstance();
    //         cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
    //         Date currentDate = cal.getTime();

    //     if (oldStatus == RentalStatus.RENT_WAIT.getValue() && status == RentalStatus.RENTAlING.getValue()){
    //         if(!expectedReturnOn.equals(currentDate)){

    //             return Optional.of("貸出予定日のみ貸出中を選択できます");
        
    //         }else{
    //             return Optional.empty(); // 正しい場合は空のOptionalを返す
    //         }
    //     }
    //     return Optional.empty();
    // }
