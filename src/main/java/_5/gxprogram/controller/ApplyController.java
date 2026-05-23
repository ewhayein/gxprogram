package _5.gxprogram.controller;

import _5.gxprogram.dto.ReservationRequestDTO;
import _5.gxprogram.service.ApplyService;
import jakarta.validation.Valid; // (주의) Validation이 작동하려면 꼭 필요합니다!
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 1. "이 클래스는 JSON 데이터를 주고받는 API 컨트롤러입니다" 라고 선언
@RestController
// 2. 이 컨트롤러의 공통 접속 주소를 "/api/reservations"로 설정
@RequestMapping("/api/reservations")
// 3. final이 붙은 필드(Service)를 스프링이 자동으로 주입(DI)해주도록 마법을 부리는 어노테이션
@RequiredArgsConstructor
public class ApplyController {
    // 4. 시스템의 '뇌(로직)'인 ApplyService를 안전하게 연결 (의존성 주입)
    private final ApplyService applyService;

    /**
     * 3 & 4. 수강 신청(예약) POST 엔드포인트
     */
    @PostMapping
    public ResponseEntity<String> applyForCourse(@Valid @RequestBody ReservationRequestDTO requestDTO) {

        // 1. 주방장(Service)에게 DTO 안의 회원ID와 강좌ID를 넘겨 요리(예약 로직) 지시
        // -> 이 안에서 좌석 차감, 낙관적 락 발동, PENDING_PAYMENT 상태 저장 등이 일어납니다.
        applyService.applyCourse(requestDTO.getMemberId(), requestDTO.getCourseId());

        // 2. 에러(예: 좌석 부족, 동시성 충돌) 없이 로직이 끝났다면,
        // 프론트엔드에게 201 Created(생성됨) 상태 코드와 성공 메시지를 반환 (4번 단계)
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("수강 신청이 접수되어 결제 대기 상태로 전환되었습니다.");
    }

}
