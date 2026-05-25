package _5.gxprogram.service;

import _5.gxprogram.domain.*;
import _5.gxprogram.dto.OrderSummaryResponseDTO;
import _5.gxprogram.exception.BusinessException;
import _5.gxprogram.repository.AccountRepository;
import _5.gxprogram.repository.ApplyRepository;
import _5.gxprogram.service.DiscountPolicy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final ApplyRepository applyRepository;
    private final DiscountPolicy discountPolicy;
    private final AccountRepository accountRepository;

    public void confirmPayment(member member) {
        // 1. 이미 결제 완료된 헬스 정기권이 있는지 확인
        long completedHealthCount = applyRepository.countByMemberAndCategoryAndStatus(
                member, programCategory.HEALTH, applyStatus.PAYMENT_COMPLETED
        );
        if (completedHealthCount > 0) {
            throw new BusinessException("이미 이용 중인 헬스 정기권이 있습니다.");
        }

        // 2. 장바구니에 담긴 헬스 정기권 개수 확인
        long pendingHealthCount = applyRepository.countByMemberAndCategoryAndStatus(
                member, programCategory.HEALTH, applyStatus.PENDING_PAYMENT
        );
        if (pendingHealthCount > 1) {
            throw new BusinessException("헬스 정기권은 한 번에 하나만 결제할 수 있습니다.");
        }

        // ... 이후 기존의 잔액 차감 및 상태 변경 로직 ...
    }

    @Transactional(readOnly = true) // 데이터 변경 없이 조회만 하므로 성능 최적화를 위해 readOnly 적용
    public OrderSummaryResponseDTO getOrderSummary(Long memberId) {

        // 1. 특정 회원의 '결제 대기(PENDING_PAYMENT)' 상태인 예약 목록만 조회
        // (주의: ReservationStatus가 Enum이라면 "PENDING_PAYMENT" 대신 Enum 값 사용)
        List<apply> pendingReservations =
                applyRepository.findByMemberIdAndStatus(memberId, applyStatus.PENDING_PAYMENT);

        // 2. 예약 내역이 없으면 모두 0원인 빈 영수증 반환
        if (pendingReservations.isEmpty()) {
            return new OrderSummaryResponseDTO(0, 0, 0);
        }

        // 3. 총 원가 계산 (할인 적용 전 모든 프로그램의 정가 합산)
        int totalOriginalPrice = 0;
        for (apply reservation : pendingReservations) {
            totalOriginalPrice += reservation.getCourse().getProgram().getPrice();
        }

        // 4. 객체 지향 할인 정책(DiscountPolicy)을 통해 할인 금액 산출 (OCP, SRP 준수)
        int discountAmount = discountPolicy.calculateDiscountAmount(pendingReservations);

        // 5. 최종 청구 금액 계산 (원가 - 할인액)
        int finalBillingPrice = totalOriginalPrice - discountAmount;

        // 6. 계산된 결과를 DTO(영수증)에 담아 반환
        return new OrderSummaryResponseDTO(totalOriginalPrice, discountAmount, finalBillingPrice);
    }

    @Transactional // 🔥 무결성을 위한 핵심! 로직 중 하나라도 실패하면 모두 롤백(Rollback)됩니다.
    public void processPayment(Long memberId) {

        // 1. 결제 대기 중인 예약 목록 다시 가져오기 (가계산 때와 동일)
        List<apply> pendingReservations =
                applyRepository.findByMemberIdAndStatus(memberId, applyStatus.PENDING_PAYMENT);

        if (pendingReservations.isEmpty()) {
            throw new IllegalStateException("결제할 내역이 존재하지 않습니다.");
        }

        // 2. 예약별 할인 분배 계산 (서버에서 다시 깐깐하게 계산 - 보안)
        Map<apply, Integer> discountMap = discountPolicy.calculateDiscountPerReservation(pendingReservations);

        // 3. 총 원가 / 총 할인 / 최종 청구액 산출
        int totalOriginalPrice = 0;
        for (apply reservation : pendingReservations) {
            totalOriginalPrice += reservation.getCourse().getProgram().getPrice();
        }
        int discountAmount = discountMap.values().stream().mapToInt(Integer::intValue).sum();
        int finalBillingPrice = totalOriginalPrice - discountAmount;

        // 4. 회원의 계좌(Account) 정보 조회
        account account = accountRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원의 계좌 정보를 찾을 수 없습니다."));

        // 5. [검증] 잔고 부족 시 예외(Exception) 발생 -> 여기서 에러가 터지면 @Transactional에 의해 자동 롤백됨!
        if (account.getBalance() < finalBillingPrice) {
            throw new IllegalStateException("잔고가 부족합니다. (현재 잔액: " + account.getBalance() + "원)");
        }

        // 6. [확정 - 계좌 차감] 잔액이 충분하므로 계좌에서 최종 결제 금액을 뺍니다.
        account.setBalance(account.getBalance() - finalBillingPrice);

        // 7. [확정] 각 예약에 결제 금액 / 상태 / 완료 시간 기록 (환불 시 paymentAmount 재사용)
        int paymentAmountSum = 0;
        LocalDateTime now = LocalDateTime.now();
        for (apply reservation : pendingReservations) {
            int originalPrice = reservation.getCourse().getProgram().getPrice();
            int perDiscount = discountMap.getOrDefault(reservation, 0);
            int paymentAmount = originalPrice - perDiscount;

            reservation.setPaymentAmount(paymentAmount);
            reservation.setStatus(applyStatus.PAYMENT_COMPLETED);
            reservation.setPaymentCompletedAt(now);

            paymentAmountSum += paymentAmount;
        }

        // 8. [무결성 검증] 분배 합계 == 최종 청구액 (불일치 시 트랜잭션 롤백)
        if (paymentAmountSum != finalBillingPrice) {
            throw new IllegalStateException(
                    "결제 금액 분배 합계가 최종 청구액과 일치하지 않습니다. (합계=" + paymentAmountSum + ", 청구액=" + finalBillingPrice + ")");
        }

        // (참고) JPA의 '더티 체킹(Dirty Checking)' 덕분에 save() 메서드를 따로 호출하지 않아도
        // @Transactional이 끝나는 시점에 변경된 잔액과 상태가 DB에 자동으로 UPDATE 됩니다.
    }
}
