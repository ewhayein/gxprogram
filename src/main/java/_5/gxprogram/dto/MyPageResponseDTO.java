package _5.gxprogram.dto;

import _5.gxprogram.domain.memberRole;
import _5.gxprogram.domain.memberStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/* 마이페이지 응답 DTO , 회원 기본정보 + 예약/결제 내역을 한 번에 전달 */

@Getter @Builder
public class MyPageResponseDTO {

    // 회원 기본 정보
    private Long memberId;
    private String studentId;
    private String name;
    private String major;
    private memberRole role;
    private memberStatus status;

    // 예약/결제 내역 목록 (A파트에서 만든 apply 엔티티 기반)
    private List<ApplyHistoryDTO> applyHistories;

    /* 예약/결제 내역 내부 DTO */
    @Getter
    @Builder
    public static class ApplyHistoryDTO {
        private Long reservationId;
        private String programName;     // 프로그램명
        private String courseInfo;      // 강좌 요약 (예: "월수금 09:00~10:00 / 김강사")
        private String status;          // 상태 (IN_CART, PAYMENT_COMPLETED 등)
        private Integer paymentAmount;  // 결제 금액
        private String createdAt;       // 신청 일시 (포맷팅된 문자열)
        private String targetDate;      // 수강 날짜 (포맷팅된 문자열)
    }
}
