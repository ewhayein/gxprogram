package _5.gxprogram.domain;

public enum    applyStatus {
    IN_CART,            // 장바구니에 담긴 상태 (추가됨)
    WAITING,            // 예약 대기
    APPLIED,            // 신청 완료
    PAYMENT_COMPLETED,  // 결제 완료
    CANCELLED,
    PENDING_PAYMENT,
    REFUNDED
}
