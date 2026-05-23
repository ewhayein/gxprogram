package _5.gxprogram.domain;

public enum applyStatus {
    IN_CART,            // 장바구니에 담긴 상태 (임시 저장에 해당)
    PAYMENT_COMPLETED,  // 결제 완료
    CANCELLED, // 예약 취소
    PENDING_PAYMENT, // 결제 진행중 = 좌석 예약
    REFUNDED // 환불
}
