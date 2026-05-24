// 주문 내역 요약
package _5.gxprogram.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

/* 결제 내역 요약 응답 DTO (마이페이지 주문 내역용) */

@Getter @Setter
public class OrderSummaryResponseDTO {
    private Long reservationId;
    private String programName;
    private String centerName;          // 센터명 (한글 변환)
    private String categoryName;        // 종목명 (한글 변환)
    private String instructorName;      // 강사명
    private String dayOfWeek;           // 요일
    private String timeInfo;            // "09:00 ~ 10:00" 형식
    private Integer paymentAmount;      // 결제 금액
    private String paymentStatus;       // 결제 상태 (한글)
    private String paymentCompletedAt;  // 결제 완료 일시
}
