package _5.gxprogram.controller;

import _5.gxprogram.dto.ReservationRequestDTO;
import _5.gxprogram.service.ApplyService;
import _5.gxprogram.dto.PaymentRequestDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ApplyController {
    private final ApplyService applyService;

    @PostMapping
    public ResponseEntity<String> applyForCourse(@Valid @RequestBody ReservationRequestDTO requestDTO) {
        applyService.applyCourse(requestDTO.getCourseId(), requestDTO.getMemberId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("강좌가 장바구니에 담겼습니다.");   // ⭐ "결제 대기" → "장바구니"
    }

    /** 장바구니에서 선택한 항목들을 일괄 결제 신청 (IN_CART → PENDING_PAYMENT) */
    @PostMapping("/request-payment")
    public ResponseEntity<String> requestPayment(@Valid @RequestBody PaymentRequestDTO dto) {
        applyService.requestPayment(dto.getMemberId(), dto.getApplyIds());
        return ResponseEntity.ok("선택한 강좌가 결제 대기 상태로 전환되었습니다. 10분 이내에 결제를 완료해주세요.");
    }
}
