package jp.co.metateam.library.model;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Optional;

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

    @NotEmpty(message="在庫管理番号は必須です")
    private String stockId;

    @NotEmpty(message="社員番号は必須です")
    private String employeeId;

    @NotNull(message="貸出ステータスは必須です")
    private Integer status;

    @DateTimeFormat(pattern="yyyy-MM-dd")
    @NotNull(message="貸出予定日は必須です")
    private Date expectedRentalOn;

    @DateTimeFormat(pattern="yyyy-MM-dd")
    @NotNull(message="返却予定日は必須です")
    private Date expectedReturnOn;

    private Timestamp rentaledAt;

    private Timestamp returnedAt;

    private Timestamp canceledAt;

    private Stock stock;

    private Account account;


    public Optional<String> ValidDateTime(Date expectedRentalOn , Date expectedReturnOn) {
        if (expectedRentalOn.compareTo(expectedReturnOn) >= 0) {
            return Optional.of("返却予定日は貸出予定日より後の日付を入力してください");
        }
        return Optional.empty();
    }

    
    public Optional<String> ValidStatus(Integer preStatus, Integer newStatus) {
        String errorMessage = "「%s」の場合は貸出ステータスを「%s」に変更できません";
        RentalStatus preRentalStatus = RentalStatus.get(preStatus);
        RentalStatus newRentalStatus = RentalStatus.get(newStatus);
    
        if (!preStatus.equals(newRentalStatus)) {
            switch (preRentalStatus) {
                case RentalStatus.RENT_WAIT:
                    if (RentalStatus.RETURNED.getValue().equals(newRentalStatus)) {
                        return Optional.of(String.format(errorMessage, preRentalStatus.getText(), newRentalStatus.getText()));
                    }
                    break;
                case RentalStatus.RENTALING:
                    if (RentalStatus.RENT_WAIT.getValue().equals(newRentalStatus)) {
                        return Optional.of(String.format(errorMessage, preRentalStatus.getText(), newRentalStatus.getText()));
                    }
                    break;
                case RentalStatus.RETURNED:
                case RentalStatus.CANCELED:
                    return Optional.of(String.format("「%s」の場合は貸出ステータスを変更できません", preRentalStatus.getText()));
            }
        }
        return Optional.empty(); // エラーメッセージがない場合
    }
    



}
