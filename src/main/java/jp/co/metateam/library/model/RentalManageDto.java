package jp.co.metateam.library.model;

import java.sql.Timestamp;
import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.AssertTrue;
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


    
    public String isValidStatus(Integer preStatus) {
        if(preStatus == RentalStatus.RENT_WAIT.getValue() &&  this.status ==  RentalStatus.RETURNED.getValue()){
          return "貸出ステータスは「貸出待ち」から「返却済み」に変更できません";
        }else if(preStatus == RentalStatus.RENTAlING.getValue() && this.status == RentalStatus.RENT_WAIT.getValue()){
          return "貸出ステータスは「貸出中」から「貸出待ち」に変更できません";
        }else if(preStatus == RentalStatus.RENTAlING.getValue() && this.status == RentalStatus.CANCELED.getValue()){
          return "貸出ステータスは「貸出中」から「キャンセル」に変更できません";
        }else if(preStatus == RentalStatus.RETURNED.getValue() && this.status == RentalStatus.RENT_WAIT.getValue()){
          return "貸出ステータスは「返却済み」から「貸出待ち」に変更はできません";
        }else if(preStatus == RentalStatus.RETURNED.getValue() && this.status == RentalStatus.RENTAlING.getValue()){
          return "貸出ステータスは「返却済み」から「貸出中」に変更はできません";
        }else if(preStatus == RentalStatus.RETURNED.getValue() && this.status == RentalStatus.CANCELED.getValue()){
          return "貸出ステータスは「返却済み」から「キャンセル」に変更はできません";
        }else if(preStatus == RentalStatus.CANCELED.getValue() && this.status == RentalStatus.RENT_WAIT.getValue()){
          return "貸出ステータスは「キャンセル」から「貸出待ち」に変更はできません";
        }else if(preStatus == RentalStatus.CANCELED.getValue() && this.status == RentalStatus.RENTAlING.getValue()){
         return "貸出ステータスは「キャンセル」から「貸出中」に変更はできません";
        }else if(preStatus == RentalStatus.CANCELED.getValue() && this.status == RentalStatus.RETURNED.getValue()){
         return "貸出ステータスは「キャンセル」から「貸出済み」に変更はできません";

        } return null;
  }   
 }
