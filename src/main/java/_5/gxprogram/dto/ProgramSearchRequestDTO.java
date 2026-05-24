package _5.gxprogram.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

import _5.gxprogram.domain.centerType;
import _5.gxprogram.domain.programCategory;

// 강좌 검색 조건 DTO
@Getter @Setter
public class ProgramSearchRequestDTO {
    private centerType center;
    private programCategory category;
    private String keyword;
    private String dayOfWeek;
    private String startTimeFrom;
    private String startTimeTo;
    private Integer maxPrice;
    private Boolean hasAvailableSlot;
}
