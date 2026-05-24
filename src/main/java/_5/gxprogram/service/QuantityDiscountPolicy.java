package _5.gxprogram.service;

import _5.gxprogram.domain.apply;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 피트니스 센터 종합 할인 정책 구현체
 * - GX/SPORTS + 헬스 동시 등록 시: 헬스 20% 할인 (기타 할인 무효화)
 * - GX/SPORTS 다수 등록 시: 2개 5%, 3개 이상 10% 할인
 */@Component
public class QuantityDiscountPolicy implements DiscountPolicy {
    @Override
    public int calculateDiscountAmount(List<apply> reservations) {
        if (reservations == null || reservations.isEmpty()) {
            return 0;
        }

        boolean hasHealth = false;
        int healthTotalPrice = 0;

        // GX와 SPORTS를 포괄하는 변수명으로 변경 (헬스 외 카테고리)
        int nonHealthCount = 0;
        int nonHealthTotalPrice = 0;

        // 1. 장바구니 내 프로그램 분류 (헬스 vs 헬스 외)
        for (apply reservation : reservations) {
            String programType = reservation.getCourse().getProgram().getCategory().name();
            int price = reservation.getCourse().getProgram().getPrice();

            if ("헬스".equals(programType) || "HEALTH".equals(programType)) {
                hasHealth = true;
                healthTotalPrice += price;
            } else {
                // 헬스가 아니면 GX 또는 SPORTS로 간주하여 함께 집계
                nonHealthCount++;
                nonHealthTotalPrice += price;
            }
        }

        // 2. 할인 로직 적용 (우선순위 기반)

        // [규칙 1 & 3] 헬스 + 타 프로그램(GX, SPORTS) 동시 등록 시: 헬스 20% 할인
        if (hasHealth && nonHealthCount > 0) {
            return (int) (healthTotalPrice * 0.20);
        }

        // [규칙 2] 헬스 없이 GX/SPORTS 프로그램만 등록 시 (수량 할인 적용)
        if (!hasHealth) {
            if (nonHealthCount >= 3) {
                return (int) (nonHealthTotalPrice * 0.10);
            } else if (nonHealthCount == 2) {
                return (int) (nonHealthTotalPrice * 0.05);
            }
        }

        return 0;
    }
}
