package _5.gxprogram.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
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
}
