package _5.gxprogram.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import _5.gxprogram.domain.applyStatus;
import lombok.Setter;
// import _5.gxprogram.domain.Member; // Member 엔티티 import

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class apply {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private course course;

    private LocalDate targetDate; // 예: 2026-06-16 (정규권은 null 허용 또는 시작일 저장)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private applyStatus status; // 상태로 장바구니와 결제완료를 구분!

    @Column(nullable = false)
    private Integer paymentAmount; // 결제 예정 금액 or 실제 결제된 금액

    private LocalDateTime createdAt; // 담은 일시 or 신청 일시 (꼭 필요할까? 통계용 아니면 확인용?)

    private LocalDateTime expiresAt; // 결제 만료 일시 (결제 대기 상태로 넘어갈 때 '현재시간 + 10분' 세팅)

    private LocalDateTime paymentCompletedAt; //결제 완료 일시 (꼭 필요할까? 환불할 때 아마)

    // --- 비즈니스 로직 및 생성자 ---


    public apply(course course, member member) {
        this.course = course;
        this.member = member;
        // ✅ applyStatus의 PENDING_PAYMENT 사용
        this.status = applyStatus.PENDING_PAYMENT;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusMinutes(10);
        this.paymentAmount = 0;
    }

    // 결제 전 단순 취소 (결제 대기 상태에서만 가능)
    public void cancel() {
        if (this.status == applyStatus.PAYMENT_COMPLETED) {
            throw new IllegalStateException("결제가 완료된 내역입니다. 단순 취소가 아닌 환불 절차를 진행해 주세요.");
        }
        if (this.status == applyStatus.REFUNDED) {
            throw new IllegalStateException("이미 환불 처리가 완료된 내역입니다.");
        }
        this.status = applyStatus.CANCELLED;
    }

    // 최종 결제 완료 처리
    public void completePayment() {
        if (this.status != applyStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("결제 대기 상태인 예약만 결제할 수 있습니다.");
        }
        this.status = applyStatus.PAYMENT_COMPLETED;
    }

    // 결제 완료 후 환불 처리
    public void refund() {
        if (this.status != applyStatus.PAYMENT_COMPLETED) {
            throw new IllegalStateException("결제가 완료된 내역만 환불할 수 있습니다.");
        }
        this.status = applyStatus.REFUNDED;
    }
}
