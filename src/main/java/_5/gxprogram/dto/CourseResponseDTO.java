package _5.gxprogram.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;
import _5.gxprogram.domain.centerType;
import _5.gxprogram.domain.course;
import _5.gxprogram.domain.program;
import _5.gxprogram.domain.programCategory;
import _5.gxprogram.domain.programStatus;

import java.time.LocalTime;

@Getter @Setter

public class CourseResponseDTO {

    // program 정보
    private Long programId;
    private String programName;
    private centerType center;
    private programCategory category;
    private Integer price;
    private String difficulty;
    private String remarks;

    // course 정보
    private Long courseId;
    private String instructorName;
    private String dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer maxCapacity;
    private Integer currentCapacity;
    private Integer remainingSlots;     // 잔여석 (계산 필드)
    private programStatus status;

    // QueryDSL Projections 용 생성자
    public CourseResponseDTO(
            Long programId, String programName, centerType center,
            programCategory category, Integer price, String difficulty, String remarks,
            Long courseId, String instructorName, String dayOfWeek,
            LocalTime startTime, LocalTime endTime,
            Integer maxCapacity, Integer currentCapacity, programStatus status) {
        this.programId = programId;
        this.programName = programName;
        this.center = center;
        this.category = category;
        this.price = price;
        this.difficulty = difficulty;
        this.remarks = remarks;
        this.courseId = courseId;
        this.instructorName = instructorName;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.maxCapacity = maxCapacity;
        this.currentCapacity = currentCapacity;
        this.remainingSlots = maxCapacity - currentCapacity;
        this.status = status;
    }
}

