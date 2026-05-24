package _5.gxprogram.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

// 로그인 요청 DTO
@Getter @Setter
public class LoginRequestDTO {
    private String studentId;   // 로그인 아이디
    private String password;
}
