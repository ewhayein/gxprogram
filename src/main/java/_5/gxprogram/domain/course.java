package _5.gxprogram.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(indexes = {
        @Index(name = "idx_course_status", columnList = "status"),
        @Index(name = "idx_course_program_id", columnList = "program_id"),
        @Index(name = "idx_course_day_of_week", columnList = "dayOfWeek"),
        @Index(name = "idx_course_start_time", columnList = "startTime")
})

public class course {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_id")

    private Long id;
    private String name;
    private int availableSeats;
    private LocalDate targetDate;

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

    // 동시성 제어(낙관적 락)를 위한 버전 관리
    @Version
    private Long version;

    public course(String name, int availableSeats) {
        this.name = name;
        this.availableSeats = availableSeats;
    }

    public course(String name, Integer maxCapacity, int availableSeats) {
        this.name = name;
        this.maxCapacity = maxCapacity;
        this.availableSeats = availableSeats;
        this.currentCapacity = 0; // 처음 개설될 때 현재 신청 인원은 0명으로 초기화
    }

    // [추가] 테스트 환경에서 강좌를 쉽게 만들기 위한 생성자
    public course(int maxSeats, int currentSeats, int availableSeats) {
        this.maxCapacity = maxSeats;
        this.currentCapacity = currentSeats;
        this.availableSeats = availableSeats;
    }

    // --- 비즈니스 로직 ---

    /**
     * 수강 신청 시 잔여 좌석 차감
     */
    // Course.java 내부 메서드
    public void decreaseSeats() {
        // 잔여 좌석이 0 이하면 신청 불가 (현재 좌석이 0개면, 즉 꽉 찼으면 에러)
        if (this.availableSeats <= 0) {
            throw new IllegalStateException("강좌가 마감되었습니다. 잔여 좌석이 없습니다.");
        }
        this.availableSeats--;
        this.currentCapacity++;
    }

    public void increaseSeats() {
        // 최대 정원을 넘어서 늘어날 수는 없음
        if (this.availableSeats >= this.maxCapacity) {
            throw new IllegalStateException("잔여 좌석이 최대 수용 인원을 초과할 수 없습니다.");
        }

        // 2. 수강생이 0명 이하인데 취소하는 경우 방어 로직 (데이터 무결성)
        if (this.currentCapacity <= 0) {
            throw new IllegalStateException("현재 수강생이 없습니다.");
        }
        this.availableSeats++;
        this.currentCapacity--;
    }
}
