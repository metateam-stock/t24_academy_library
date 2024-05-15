package jp.co.metateam.library.values;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RentalStatus implements Values {
    RENT_WAIT(0, "貸出待ち")
    , RENTAlING(1, "貸出中")
    , RETURNED(2, "返却済み")
    , CANCELED(3, "キャンセル");

    private final Integer value;
    private final String text;  

    public static String getText(Integer value) {           //メソッドは Integer 型の引数 value を受け取る、これは貸出ステータスを表す整数値
        for (RentalStatus status : RentalStatus.values()) {         //メソッドは RentalStatus 列挙型の各要素に対してループを実行。RentalStatus.values() は、RentalStatus 列挙型のすべての要素を配列として返す。
            if (status.getValue().equals(value)) {          //各要素に対して、そのステータスの整数値を取得し、引数で渡された値と比較
                return status.getText();            //引数で渡された値と一致するステータスが見つかった場合、そのステータスのテキストを返す
            }
        }
        return null;            //一致するステータスが見つからない場合は、null を返す
    }
    
}


