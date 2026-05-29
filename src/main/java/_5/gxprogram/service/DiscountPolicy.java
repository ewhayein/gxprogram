package _5.gxprogram.service;

import _5.gxprogram.domain.apply;
import java.util.List;
import java.util.Map;

public interface DiscountPolicy {

    // 예약별 할인 금액 산출
    Map<apply, Integer> calculateDiscountPerReservation(List<apply> reservations);

    // 총 할인 금액 산출
    default int calculateDiscountAmount(List<apply> reservations) {
        return calculateDiscountPerReservation(reservations)
                .values().stream()
                .mapToInt(Integer::intValue)
                .sum();
    }
}
