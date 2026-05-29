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
        // 이미 결제 완료된 헬스 정기권이 있는지 확인
        long completedHealthCount = applyRepository.countByMemberAndCategoryAndStatus(
                member, programCategory.HEALTH, applyStatus.PAYMENT_COMPLETED
        );
        if (completedHealthCount > 0) {
            throw new BusinessException("이미 이용 중인 헬스 정기권이 있습니다.");
        }

        // 장바구니에 담긴 헬스 정기권 개수 확인
        long pendingHealthCount = applyRepository.countByMemberAndCategoryAndStatus(
                member, programCategory.HEALTH, applyStatus.PENDING_PAYMENT
        );
        if (pendingHealthCount > 1) {
            throw new BusinessException("헬스 정기권은 한 번에 하나만 결제할 수 있습니다.");
        }

        //이후 기존의 잔액 차감 및 상태 변경 로직
    }

    @Transactional(readOnly = true)
    public OrderSummaryResponseDTO getOrderSummary(Long memberId) {

        // 결제 대기 목록 조회
        List<apply> pendingReservations =
                applyRepository.findByMemberIdAndStatus(memberId, applyStatus.PENDING_PAYMENT);

        // 예약 내역이 없으면 빈 영수증 반환
        if (pendingReservations.isEmpty()) {
            return new OrderSummaryResponseDTO(0, 0, 0);
        }

        // 총 원가 계산
        int totalOriginalPrice = 0;
        for (apply reservation : pendingReservations) {
            totalOriginalPrice += reservation.getCourse().getProgram().getPrice();
        }

        // 할인 금액 계산
        int discountAmount = discountPolicy.calculateDiscountAmount(pendingReservations);

        // 최종 청구 금액 계산
        int finalBillingPrice = totalOriginalPrice - discountAmount;

        return new OrderSummaryResponseDTO(totalOriginalPrice, discountAmount, finalBillingPrice);
    }

    @Transactional
    public void processPayment(Long memberId) {

        // 결제 대기 목록 조희
        List<apply> pendingReservations =
                applyRepository.findByMemberIdAndStatus(memberId, applyStatus.PENDING_PAYMENT);

        if (pendingReservations.isEmpty()) {
            throw new IllegalStateException("결제할 내역이 존재하지 않습니다.");
        }

        // 할인 분배 계산
        Map<apply, Integer> discountMap = discountPolicy.calculateDiscountPerReservation(pendingReservations);

        // 금액 계산
        int totalOriginalPrice = 0;
        for (apply reservation : pendingReservations) {
            totalOriginalPrice += reservation.getCourse().getProgram().getPrice();
        }
        int discountAmount = discountMap.values().stream().mapToInt(Integer::intValue).sum();
        int finalBillingPrice = totalOriginalPrice - discountAmount;

        // 계좌 조회
        account account = accountRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원의 계좌 정보를 찾을 수 없습니다."));

        // 잔고 검증
        if (account.getBalance() < finalBillingPrice) {
            throw new IllegalStateException("잔고가 부족합니다. (현재 잔액: " + account.getBalance() + "원)");
        }

        // 확정 - 계좌 차감
        account.setBalance(account.getBalance() - finalBillingPrice);

        // 결제 완료 처리
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

        // 무결성 검증
        if (paymentAmountSum != finalBillingPrice) {
            throw new IllegalStateException(
                    "결제 금액 분배 합계가 최종 청구액과 일치하지 않습니다. (합계=" + paymentAmountSum + ", 청구액=" + finalBillingPrice + ")");
        }
    }
}
