package jp.co.metateam.library.model;

import java.sql.Timestamp;
import java.util.Date;

import org.hibernate.sql.Insert;
import org.hibernate.sql.ast.tree.insert.Values;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import jp.co.metateam.library.values.RentalStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import jp.co.metateam.library.model.Account;//
import jp.co.metateam.library.model.RentalManage;
import jp.co.metateam.library.model.RentalManageDto;
import jp.co.metateam.library.model.Stock;
import jp.co.metateam.library.repository.AccountRepository;
import jp.co.metateam.library.repository.RentalManageRepository;
import jp.co.metateam.library.repository.StockRepository;
import jp.co.metateam.library.values.RentalStatus;




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

    public String isValidStatus(Integer preStatus, Integer newStatus) {
        if(preStatus != newStatus) {

             
            switch(preStatus) {
                case 0:
                    if (newStatus == 2) {
                        return String.format("貸出ステータスは「貸出待ち」から「返却済み」に変更できません");
                    }
                break;
                case 1:
                    if(newStatus == 0) {
                        return String.format("貸出ステータスは「貸出中」から「貸出待ち」に変更できません");
                    } else if (newStatus == 3) {
                        return String.format("貸出ステータスは「貸出中」から「キャンセル」に変更できません");
                    } 
                    break;
                case 2:
                    if(newStatus == 0){
                        return String.format("貸出ステータスは「返却済み」から「貸出待ち」に変更できません");
                    } else if(newStatus == 1){
                        return String.format("貸出ステータスは「返却済み」から「貸出中」に変更できません");
                    } else if(newStatus == 3){
                        return String.format("貸出ステータスは「返却済み」から「キャンセル」に変更できません");
                    } 
                    break;                   
                case 3:
                   if(newStatus == 0){
                       return String.format("貸出ステータスは「キャンセル」から「貸出待ち」に変更できません");
                    } else if(newStatus == 1){
                       return String.format("貸出ステータスは「キャンセル」から「貸出中」に変更できません");
                    } else if(newStatus == 2){
                       return String.format("貸出ステータスは「キャンセル」から「返却済み」に変更できません");
                    } 
                    break; 
            }
        }
        return null;
    }
}
    


    
    
    
    
    
    
    
   