package jp.co.metateam.library.model;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jp.co.metateam.library.constants.Constants;
import jp.co.metateam.library.repository.RentalManageRepository;
import jp.co.metateam.library.service.RentalManageService;
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
    private RentalManageRepository rentalManageRepository;
    private RentalManage rentalManage;
    private RentalManageService rentalManageService;

    // nullか確認
    @NotEmpty(message = "在庫管理番号は必須です")
    private String stockId;

    @NotEmpty(message = "社員番号は必須です")
    private String employeeId;

    @NotNull(message = "貸出ステータスは必須です")
    private Integer status;

    // フォーマットとnull可の確認
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "貸出予定日は必須です")
    private Date expectedRentalOn;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "返却予定日は必須です")
    private Date expectedReturnOn;

    public String rentalAddStatusError() {
        if (this.status == RentalStatus.RETURNED.getValue()) {
            return "貸出登録で「返却済み」は選択できません";
        }
        if (this.status == RentalStatus.CANCELED.getValue()) {
            return "貸出登録で「キャンセル」は選択できません";
        }
        return null;
    }

    // 貸出日<返却日
    public String dateRentalReturnError() {
        if (this.expectedRentalOn.compareTo(expectedReturnOn) > 0) {
            return "「返却予定日」は「貸出予定日」より後の日時を選択してください";
        }
        return null;
    }

    public String isStatusError(Integer pastStatus) {
        // 変更前のステータスが貸出待ちのとき
        if (pastStatus == RentalStatus.RENT_WAIT.getValue()) {
            if (this.status == RentalStatus.RETURNED.getValue()) {
                return "「貸出待ち」から「返却済み」は選択できません";
            }
        }
        // 変更前のステータスが貸出中のとき
        if (pastStatus == RentalStatus.RENTALING.getValue()) {
            if (this.status == RentalStatus.RENT_WAIT.getValue()) {
                return "「貸出中」から「貸出待ち」は選択できません";
            }
            if (this.status == RentalStatus.CANCELED.getValue()) {
                return "「貸出中」から「キャンセル」は選択できません";
            }
        }
        // 変更前のステータスが返却済みのとき
        if (pastStatus == RentalStatus.RETURNED.getValue()) {
            if (this.status == RentalStatus.RENT_WAIT.getValue()) {
                return "「返却済み」から「貸出待ち」は選択できません";
            }
            if (this.status == RentalStatus.RENTALING.getValue()) {
                return "「返却済み」から「貸出中」は選択できません";
            }
            if (this.status == RentalStatus.CANCELED.getValue()) {
                return "「返却済み」から「貸出待ち」は選択できません";
            }
        }
        // 変更前のステータスがキャンセルのとき
        if (pastStatus == RentalStatus.CANCELED.getValue()) {
            if (this.status == RentalStatus.RENT_WAIT.getValue()) {
                return "「キャンセル」から「貸出待ち」は選択できません";
            }
            if (this.status == RentalStatus.RENTALING.getValue()) {
                return "「キャンセル」から「貸出中」は選択できません";
            }
            if (this.status == RentalStatus.RETURNED.getValue()) {
                return "「キャンセル」から「返却済み」は選択できません";
            }
        }
        return null;
    }

    private Timestamp rentaledAt;

    private Timestamp returnedAt;

    private Timestamp canceledAt;

    private Stock stock;

    private Account account;
}
