// 주문 내역 요약
package _5.gxprogram.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 프론트엔드 결제 확인창에 보여줄 영수증 DTO
 */
@Getter
@AllArgsConstructor
public class OrderSummaryResponseDTO {

    private int totalOriginalPrice; // 총 결제 금액 (할인 전 원가 종합)
    private int discountAmount;     // 총 할인 금액 (DiscountPolicy가 계산해준 값)
    private int finalBillingPrice;  // 최종 청구 금액 (실제 회원의 계좌에서 빠져나갈 돈)

    // 필요하다면 나중에 List<String> programNames 같은 필드를 추가해
    // 어떤 강좌들이 결제 대기 중인지 이름 목록을 함께 넘겨줄 수도 있습니다.
}
