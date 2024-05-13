package jp.co.metateam.library.model;

import java.sql.Timestamp;
import java.util.Date;

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
    
    //nullか確認
    @NotEmpty(message="在庫管理番号は必須です")
    private String stockId;

    @NotEmpty(message="社員番号は必須です")
    private String employeeId;

    @NotNull(message="貸出ステータスは必須です")
    private Integer status;

    //フォーマットとnull可の確認
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @NotNull(message="貸出予定日は必須です")
    private Date expectedRentalOn;


    @DateTimeFormat(pattern="yyyy-MM-dd")
    @NotNull(message="返却予定日は必須です")
    private Date expectedReturnOn;

    public String isStatusError(Integer preStatus){
        if (preStatus==RentalStatus.RENT_WAIT.getValue() && this.status==RentalStatus.RETURNED.getValue()) {
            //変更前の貸出ステータスが貸出待ち　かつ　（この）画面から選択されたステータスが貸出待ちのときは"エラー文"を返す
            return "「貸出待ち」から「返却済み」は選択できません";
        }else if(preStatus==RentalStatus.RENTAlING.getValue() && this.status==RentalStatus.RENT_WAIT.getValue()){
            return "「貸出中」から「貸出待ち」は選択できません";
        }else if(preStatus==RentalStatus.RENTAlING.getValue() && this.status==RentalStatus.CANCELED.getValue()){
            return "「貸出中」から「キャンセル」は選択できません";
        }else if(preStatus==RentalStatus.RETURNED.getValue() && this.status==RentalStatus.RENT_WAIT.getValue()){
            return "「返却済み」から「貸出待ち」は選択できません";
        }else if(preStatus==RentalStatus.RETURNED.getValue() && this.status==RentalStatus.RENTAlING.getValue()){
            return "「返却済み」から「貸出中」は選択できません";
        }else if(preStatus==RentalStatus.RETURNED.getValue() && this.status==RentalStatus.CANCELED.getValue()){
            return "「返却済み」から「貸出待ち」は選択できません";
        }else if(preStatus==RentalStatus.CANCELED.getValue() && this.status==RentalStatus.RENT_WAIT.getValue()){
            return "「キャンセル」から「貸出待ち」は選択できません";
        }else if(preStatus==RentalStatus.CANCELED.getValue() && this.status==RentalStatus.RENTAlING.getValue()){
            return "「キャンセル」から「貸出中」は選択できません";
        }else if(preStatus==RentalStatus.CANCELED.getValue() && this.status==RentalStatus.RETURNED.getValue()){
            return "「キャンセル」から「返却済み」は選択できません";
        }else{
            return null;
        }
    }


    private Timestamp rentaledAt;

    private Timestamp returnedAt;

    private Timestamp canceledAt;

    private Stock stock;

    private Account account;
}
