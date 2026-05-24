package _5.gxprogram.dto;

import _5.gxprogram.domain.memberRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

// 회원 가입 DTO
@Getter @Setter
public class MemberSignupRequestDTO {
    @NotBlank(message = "학번을 입력해주세요.")
    private String studentId;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    private String password;

    @NotBlank(message = "이름을 입력해주세요.")
    private String name;

    private String major;

    private String familyName;      // 직계가족 학번

    @NotNull(message = "회원 유형을 선택해주세요.")
    private memberRole role;
}
