package _5.gxprogram.service;

import _5.gxprogram.domain.apply;
import java.util.List;
import java.util.Map;

public interface DiscountPolicy {

    /**
     * 각 예약(apply)별 할인 금액을 산출합니다.
     * 할인이 적용되지 않는 예약은 결과 맵에 포함되지 않을 수 있습니다.
     *
     * @param reservations 회원의 결제 대기 상태(PENDING_PAYMENT)인 예약(장바구니) 목록
     * @return 예약(apply) -> 할인 금액(원) 매핑
     */
    Map<apply, Integer> calculateDiscountPerReservation(List<apply> reservations);

    /**
     * 결제 대기 중인 예약 목록을 분석하여 총 할인 금액을 산출합니다.
     * (기본 구현: 예약별 할인 금액의 합계)
     */
    default int calculateDiscountAmount(List<apply> reservations) {
        return calculateDiscountPerReservation(reservations)
                .values().stream()
                .mapToInt(Integer::intValue)
                .sum();
    }
}
