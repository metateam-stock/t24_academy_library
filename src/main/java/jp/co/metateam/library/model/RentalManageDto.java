package jp.co.metateam.library.model;

import java.sql.Timestamp;
import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import jp.co.metateam.library.values.RentalStatus;
import java.util.Optional;

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

    public Optional<String> isStatusError(Integer oldStatus, Integer status) {
        if (oldStatus == RentalStatus.RENT_WAIT.getValue()&& status == RentalStatus.RETURNED.getValue()) {
            return Optional.of("貸出待ちから返却済みは選択できません");
        } else if (oldStatus == RentalStatus.RENTAlING.getValue()&& status == RentalStatus.RENT_WAIT.getValue()) {
            return Optional.of("貸出中から貸出待ちは選択できません");
        }else if(oldStatus == RentalStatus.RENTAlING.getValue()&& status == RentalStatus.CANCELED.getValue()) {
            return Optional.of("貸出中からキャンセルは選択できません");
        }else if(oldStatus == RentalStatus.RETURNED.getValue()&& status == RentalStatus.RENT_WAIT.getValue()) {
            return Optional.of("貸出済みから貸出待ちは選択できません");
        }else if(oldStatus == RentalStatus.RETURNED.getValue()&& status == RentalStatus.RENTAlING.getValue()) {
            return Optional.of("貸出済みから貸出中は選択できません");
        }else if(oldStatus == RentalStatus.RETURNED.getValue()&& status == RentalStatus.CANCELED.getValue()) {
            return Optional.of("貸出済みからキャンセルは選択できません");
        }else if(oldStatus == RentalStatus.CANCELED.getValue()&& status == RentalStatus.RENT_WAIT.getValue()) {
            return Optional.of("キャンセルから貸出待ちは選択できません");
        }else if(oldStatus == RentalStatus.CANCELED.getValue()&& status == RentalStatus.RENTAlING.getValue()) {
            return Optional.of("キャンセルから貸出中は選択できません");
        }else if(oldStatus == RentalStatus.CANCELED.getValue()&& status == RentalStatus.RETURNED.getValue()) {
            return Optional.of("キャンセルから貸出済みは選択できません");
        }else{
            return Optional.empty(); // 遷移が正しい場合は空のOptionalを返す
        }
    }
    
   /**  public void updateStatus(Integer status) {
        List<RentalManage> rentalManageList = this.rentalManageRepository.findAll();
        // リスト内の各エンティティからステータスを取得
        for (RentalManage rentalManageEntity : rentalManageList) {
            Integer oldStatus = rentalManageEntity.getStatus();
            
        
        // model.addAttribute("rentalStatus", RentalStatus.values());
    
        if (oldStatus == 0) {            
            if (status == 1) {                
                // 貸出待ちから貸出中への状態遷移
                oldStatus = 1;             
            } else if (status == 3) {                
                // 貸出待ちからキャンセルへの状態遷移
                oldStatus = 3;             
            } else {                
                // 不正な状態遷移を検出した場合の処理
                System.out.println("不正な状態遷移です。");             
            }         
        } else if (oldStatus == 1) {            
            if (status == 2) {                
                // 貸出中から返却済みへの状態遷移
                oldStatus = 2;             
            } else {                
                // 不正な状態遷移を検出した場合の処理
                System.out.println("不正な状態遷移です。");             
            }         
        } else {            
            // その他の状態の場合の処理
            System.out.println("サポートされていない状態です。");         
        }     
    }
    }*/
}
