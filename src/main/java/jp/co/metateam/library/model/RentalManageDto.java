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

    
    public Optional<String> isValidStatus(Integer preStatus, Integer newStatus) {
    if (!preStatus.equals(newStatus)) {
        switch (preStatus) {
            case 0:
                if (newStatus.equals(RentalStatus.RETURNED.getValue())) {
                    return Optional.of("「貸出待ち」の場合は貸出ステータスを「返却済み」に変更できません");
                }
                break;
            case 1:
                if (newStatus.equals(RentalStatus.RENT_WAIT.getValue())) {
                    return Optional.of("「貸出中」の場合は貸出ステータスを「貸出待ち」に変更できません");
                }
                if (newStatus.equals(RentalStatus.CANCELED.getValue())) {
                    return Optional.of("「貸出中」の場合は貸出ステータスを「キャンセル」に変更できません");
                }
                break;
            case 2:
                return Optional.of("返却済みの場合は貸出ステータスを変更できません");
            case 3:
                return Optional.of("キャンセルの場合は貸出ステータスを変更できません");
        }
    }
    return Optional.empty(); // エラーメッセージがない場合
}



}
