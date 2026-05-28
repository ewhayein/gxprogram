package _5.gxprogram.dto;

import _5.gxprogram.domain.memberRole;
import _5.gxprogram.domain.memberStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

// 회원 기본정보 + 예약/결제 내역을 한 번에 전달
@Getter @Builder
public class MyPageResponseDTO {

    // 회원 기본 정보
    private Long memberId;
    private String studentId;
    private String name;
    private String major;
    private memberRole role;
    private memberStatus status;

    private Integer accountBalance;             // 계좌 잔액 추가

    // 예약/결제 내역
    private List<ApplyHistoryDTO> applyHistories;

    // 예약/결제 내역 내부 DTO
    @Getter @Builder
    public static class ApplyHistoryDTO {
        private Long reservationId;
        private String programName;
        private String courseInfo;
        private String status;
        private Integer paymentAmount;
        private String createdAt;       // 신청 일시
        private String targetDate;      // 수강 날짜
    }
}
