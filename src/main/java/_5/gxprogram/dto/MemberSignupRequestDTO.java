package _5.gxprogram.dto;

import _5.gxprogram.domain.memberRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

// 회원 가입 DTO
@Getter @Setter
public class MemberSignupRequestDTO {
    @NotBlank(message = "학번을 입력해주세요.")
    private String studentId;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\\W_]).{8,20}$",
            message = "비밀번호는 대문자·소문자·숫자·특수문자를 각각 1개 이상 포함한 8~20자리여야 합니다."
    )
    private String password;

    @NotBlank(message = "이름을 입력해주세요.")
    private String name;

    private String major;

    private String familyName;      // 직계가족 학번

    @NotNull(message = "회원 유형을 선택해주세요.")
    private memberRole role;
}
