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
public class gxProgram {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "program_id")
    private Long id; // 대체키

    @Enumerated(EnumType.STRING)
    private centerType centerType; // 센터 종류 (ECC vs 체력단련실)

    @Enumerated(EnumType.STRING)
    private programCategory category; // 종목 분류

    @Column(nullable = false)
    private String name; // 프로그램명 (예: 헬스(1개월))

    private String dayOfWeek; // 요일 (예: 월수금)
    private LocalTime startTime;
    private LocalTime endTime;

    private String difficulty; // 난이도
    private Integer price; // 금액
    private String instructorName; // 강사명

    @Column(nullable = false)
    private Integer maxCapacity; // 최대 정원

    @Column(nullable = false)
    private Integer currentCapacity; // 현재 신청 인원

    @Enumerated(EnumType.STRING)
    private programStatus status; // 폐강 여부 관리

    private String remarks; // 비고란

    @Version
    private Long version; // 동시성 제어(낙관적 락)를 위한 버전 관리
}


