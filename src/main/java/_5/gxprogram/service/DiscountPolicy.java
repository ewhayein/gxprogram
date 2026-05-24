package _5.gxprogram.service;

import _5.gxprogram.domain.apply;
import java.util.List;

public interface DiscountPolicy {
    /**
     * 결제 대기 중인 예약 목록을 분석하여 최종 할인 금액을 산출합니다.
     * * @param reservations 회원의 결제 대기 상태(PENDING_PAYMENT)인 예약(장바구니) 목록
     * @return 총 할인 금액 (원)
     */
    int calculateDiscountAmount(List<apply> reservations);
}
