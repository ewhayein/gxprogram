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
@NoArgsConstructor
public class course {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_id")
    private Long id;

    private String name;

    private int availableSeats;

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

    // --- 비즈니스 로직 ---

    /**
     * 수강 신청 시 잔여 좌석 차감
     */
    public void decreaseSeats() {
        if (this.availableSeats <= 0) {
            // 잔여 좌석이 없을 경우 예외 발생 (Custom Exception으로 변경 권장)
            throw new IllegalStateException("강좌가 마감되었습니다. 잔여 좌석이 없습니다.");
        }
        this.availableSeats--;
    }

    /**
     * 환불 시 좌석 원상복구
     */
    public void increaseSeats() {
        if (this.availableSeats >= this.maxCapacity) {
            // 최대 수용 인원 방어 로직 추가
            throw new IllegalStateException("잔여 좌석이 최대 수용 인원을 초과할 수 없습니다.");
        }
        this.availableSeats++;
    }
}
