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
public class course {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_id")
    private Long id;

    // N:1 연관관계 매핑 (어떤 프로그램에 속한 강좌인지)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id")
    private program program;

    private String instructorName; // 강사명
    private String dayOfWeek; // 요일 (예: 월수금)
    private LocalTime startTime; // 시작 시간
    private LocalTime endTime; // 종료 시간

    @Column(nullable = false)
    private Integer maxCapacity; // 최대 정원 (무제한일 경우 처리 로직 필요)

    @Column(nullable = false)
    private Integer currentCapacity = 0; // 현재 신청 인원 (기본값 0)

    @Enumerated(EnumType.STRING)
    private programStatus status; // 폐강 여부 (개별 강좌 단위로 폐강될 수 있으므로 여기에 위치)

    @Version
    private Long version; // 동시성 제어(낙관적 락)를 위한 버전 관리
}
