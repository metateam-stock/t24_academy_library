package jp.co.metateam.library.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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

    public String isStatusError(Integer pastStatus){
        if (pastStatus==RentalStatus.RENT_WAIT.getValue() && this.status==RentalStatus.RETURNED.getValue()) {
            return "「貸出待ち」から「返却済み」は選択できません";
        }else if(pastStatus==RentalStatus.RENTAlING.getValue() && this.status==RentalStatus.RENT_WAIT.getValue()){
            return "「貸出中」から「貸出待ち」は選択できません";
        }else if(pastStatus==RentalStatus.RENTAlING.getValue() && this.status==RentalStatus.CANCELED.getValue()){
            return "「貸出中」から「キャンセル」は選択できません";
        }else if(pastStatus==RentalStatus.RETURNED.getValue() && this.status==RentalStatus.RENT_WAIT.getValue()){
            return "「返却済み」から「貸出待ち」は選択できません";
        }else if(pastStatus==RentalStatus.RETURNED.getValue() && this.status==RentalStatus.RENTAlING.getValue()){
            return "「返却済み」から「貸出中」は選択できません";
        }else if(pastStatus==RentalStatus.RETURNED.getValue() && this.status==RentalStatus.CANCELED.getValue()){
            return "「返却済み」から「貸出待ち」は選択できません";
        }else if(pastStatus==RentalStatus.CANCELED.getValue() && this.status==RentalStatus.RENT_WAIT.getValue()){
            return "「キャンセル」から「貸出待ち」は選択できません";
        }else if(pastStatus==RentalStatus.CANCELED.getValue() && this.status==RentalStatus.RENTAlING.getValue()){
            return "「キャンセル」から「貸出中」は選択できません";
        }else if(pastStatus==RentalStatus.CANCELED.getValue() && this.status==RentalStatus.RETURNED.getValue()){
            return "「キャンセル」から「返却済み」は選択できません";
        }else{
            return null;
        }
    }



                                                       //Integer型の予定日           返却日の変数         在庫管理番号
    /*  public String findAvailableWithRentalDate(Integer pastExpectedRentalOn, Integer pastExpectedReturnOn,Long id) {
        //在庫管理番号に紐づくすべての貸出情報の取得
        RentalManage rentalManage = this.rentalManageService.findById(Long.valueOf(id));
        //上で取得した情報をリストに格納
        List<RentalManage> rentalManageList = new ArrayList<>();

        //リストの在庫管理番号に紐づく貸出待ちの貸出情報の取得?
        //リストの在庫管理番号に紐づく貸出中の貸出情報の取得?

        //リスト分ループする
        for (int i = 0; i < {リストの変数名}.size(); i++) {
           //条件分岐(Dtoの返却日<登録済みの貸出日 かつ Dtoの返却日>登録済みの返却日)
           if(){
            return "{エラー文}";
           }else if(){
            return "{エラー文}";
           }
           return null;
        }
    }*/


    private Timestamp rentaledAt;

    private Timestamp returnedAt;

    private Timestamp canceledAt;

    private Stock stock;

    private Account account;
}
