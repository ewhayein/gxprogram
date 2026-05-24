package _5.gxprogram.controller;

import _5.gxprogram.dto.LoginRequestDTO;
import _5.gxprogram.dto.MemberSignupRequestDTO;
import _5.gxprogram.dto.MyPageResponseDTO;
import _5.gxprogram.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    /* 회원가입 */
    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> signup(@Valid @RequestBody MemberSignupRequestDTO dto) {
        Long memberId = memberService.signup(dto);
        return ResponseEntity.ok(Map.of(
                "message", "회원가입이 완료되었습니다.",
                "memberId", memberId
        ));
    }

    /* 로그인 */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequestDTO dto) {
        Long memberId = memberService.login(dto);
        return ResponseEntity.ok(Map.of(
                "message", "로그인 성공",
                "memberId", memberId
        ));
    }

    /* 마이페이지 조회 */
    @GetMapping("/{memberId}/mypage")
    public ResponseEntity<MyPageResponseDTO> getMyPage(@PathVariable Long memberId) {
        MyPageResponseDTO response = memberService.getMyPage(memberId);
        return ResponseEntity.ok(response);
    }
}
