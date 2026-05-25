package _5.gxprogram.service;

import _5.gxprogram.domain.apply;
import _5.gxprogram.domain.programCategory;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class QuantityDiscountPolicy implements DiscountPolicy {

    //수량 할인이 적용되는 카테고리만 안전하게 그룹화
    private static final Set<programCategory> QUANTITY_DISCOUNT_TARGETS = EnumSet.of(
            programCategory.GX, programCategory.SPORTS_CLIMBING,
            programCategory.GOLF, programCategory.SQUASH,
            programCategory.TENNIS, programCategory.TABLE_TENNIS);

    @Override
    public Map<apply, Integer> calculateDiscountPerReservation(List<apply> reservations) {
        Map<apply, Integer> result = new LinkedHashMap<>();
        if (reservations == null || reservations.isEmpty()) {
            return result;
        }

        // 1차 순회: 장바구니 구성 분석
        boolean hasHealth = false;
        boolean hasComboTrigger = false; // 헬스 20% 할인을 발동시키는 트리거 (GX/스포츠/SMALL_GROUP)
        int quantityTargetCount = 0;     // 수량 할인 대상 개수

        for (apply reservation : reservations) {
            programCategory category = reservation.getCourse().getProgram().getCategory();

            if (category == programCategory.HEALTH) {
                hasHealth = true;
            } else if (QUANTITY_DISCOUNT_TARGETS.contains(category)) {
                hasComboTrigger = true;
                quantityTargetCount++;
            } else if (category == programCategory.SMALL_GROUP) {
                hasComboTrigger = true;
            }
            // ONE_TIME_PASS, MEASUREMENT는 어떤 할인에도 기여하지 않으므로 무시
        }

        // 2차 순회: 규칙에 따라 각 예약(apply)에 할인액 분배 (우선순위: 콤보 -> 수량)

        // [규칙 1] 헬스 + (GX/스포츠/SMALL_GROUP) 콤보 → 헬스 카테고리 apply에만 20% 할인
        if (hasHealth && hasComboTrigger) {
            for (apply reservation : reservations) {
                programCategory category = reservation.getCourse().getProgram().getCategory();
                if (category == programCategory.HEALTH) {
                    int price = reservation.getCourse().getProgram().getPrice();
                    result.put(reservation, (int) (price * 0.20));
                }
            }
            return result;
        }

        // [규칙 2] 헬스 없이 수량 할인 대상 다수 결제 → 수량 대상 apply에만 비율 할인
        if (!hasHealth) {
            double rate = 0.0;
            if (quantityTargetCount >= 3) {
                rate = 0.10;
            } else if (quantityTargetCount == 2) {
                rate = 0.05;
            }

            if (rate > 0.0) {
                for (apply reservation : reservations) {
                    programCategory category = reservation.getCourse().getProgram().getCategory();
                    if (QUANTITY_DISCOUNT_TARGETS.contains(category)) {
                        int price = reservation.getCourse().getProgram().getPrice();
                        result.put(reservation, (int) (price * rate));
                    }
                }
            }
        }

        // 그 외 (단건 결제, 일일권만 결제 등) - 빈 맵 반환
        return result;
    }
}
