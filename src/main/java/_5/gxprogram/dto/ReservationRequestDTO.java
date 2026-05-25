package _5.gxprogram.dto;

import jakarta.validation.constraints.NotNull; // Spring Boot 3.x 기준 (2.x는 javax.validation)
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
public class ReservationRequestDTO {
    // 수강 신청을 시도하는 회원의 고유 ID
    @NotNull(message = "회원 ID는 필수 입력값입니다.")
    private Long memberId;

    // 회원이 신청하고자 하는 강좌(Course)의 고유 ID
    @NotNull(message = "강좌 ID는 필수 입력값입니다.")
    private Long courseId;
}
