package jp.co.metateam.library.values;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RentalStatus implements Values {
    RENT_WAIT(0, "貸出待ち")
    , RENTALING(1, "貸出中")
    , RETURNED(2, "返却済み")
    , CANCELED(3, "キャンセル");

    private final Integer value;
    private final String text;  

    private static final Map<Integer, RentalStatus> map = new HashMap<Integer, RentalStatus>();
    static {
        for(RentalStatus status: RentalStatus.values()) {
            map.put(status.value, status);
        }
    }

    public static RentalStatus get(Integer value) {
        return map.getOrDefault(value, null);
    }
}
