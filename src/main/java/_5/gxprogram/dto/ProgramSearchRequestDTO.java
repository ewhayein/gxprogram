package _5.gxprogram.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

import _5.gxprogram.domain.centerType;
import _5.gxprogram.domain.programCategory;

/* 강좌 검색 조건 DTO  */

@Getter @Setter
public class ProgramSearchRequestDTO {
    private centerType center;           // 센터 (ECC_FITNESS, FITNESS_ROOM)
    private programCategory category;   // 종목 분류
    private String keyword;             // 프로그램명 키워드 검색
    private String dayOfWeek;           // 요일 (예: "월", "화수목")
    private String startTimeFrom;       // 시작 시간 범위 시작 (예: "09:00")
    private String startTimeTo;         // 시작 시간 범위 끝 (예: "12:00")
    private Integer maxPrice;           // 최대 금액 필터
    private Boolean hasAvailableSlot;   // 잔여석 있는 강좌만 조회
}
