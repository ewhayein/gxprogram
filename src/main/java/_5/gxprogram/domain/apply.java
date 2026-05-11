package _5.gxprogram.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
    @JoinColumn(name = "program_id")
    private gxProgram program;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private applyStatus status; // 상태로 장바구니와 결제완료를 구분!

    @Column(nullable = false)
    private Integer paymentAmount; // 결제 예정 금액 or 실제 결제된 금액

    private LocalDateTime createdAt; // 담은 일시 or 신청 일시
}
