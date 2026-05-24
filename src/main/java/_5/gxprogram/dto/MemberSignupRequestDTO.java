package _5.gxprogram.dto;

import _5.gxprogram.domain.memberRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/*회원 가입 요청 DTO*/

@Getter @Setter
public class MemberSignupRequestDTO {
    @NotBlank(message = "학번을 입력해주세요.")
    private String studentId;       // 학번

    @NotBlank(message = "비밀번호를 입력해주세요.")
    private String password;

    @NotBlank(message = "이름을 입력해주세요.")
    private String name;            // 이름

    private String major;           // 전공 (선택)

    private String familyName;      // 직계가족 학번 (이화가족인 경우만)

    @NotNull(message = "회원 유형을 선택해주세요.")
    private memberRole role;        // EWHA_STUDENT, ALUMNI, FACULTY, EWHA_FAMILY
}
