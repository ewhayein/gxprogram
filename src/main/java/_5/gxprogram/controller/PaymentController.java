package _5.gxprogram.controller;

import _5.gxprogram.dto.OrderSummaryResponseDTO;
import _5.gxprogram.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController // 이 클래스가 프론트엔드와 JSON 데이터로 통신하는 창구임을 선언합니다.
@RequestMapping("/api/payments") // 이 컨트롤러의 기본 주소(URL)를 설정합니다.
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * [GET] 결제 정보(영수증) 조회 API
     * 화면: 결제 대기(장바구니) 페이지 진입 시
     * URL 경로: GET /api/payments/summary/{memberId}
     */
    @GetMapping("/summary/{memberId}")
    public ResponseEntity<OrderSummaryResponseDTO> getOrderSummary(@PathVariable Long memberId) {
        // 서비스의 가계산 로직을 호출하여 결과를 가져옵니다.
        OrderSummaryResponseDTO summary = paymentService.getOrderSummary(memberId);

        // 프론트엔드에게 상태 코드 200(OK)과 함께 영수증 데이터를 반환합니다.
        return ResponseEntity.ok(summary);
    }

    /**
     * [POST] 실제 결제 승인 및 처리 API
     * 화면: "결제하기" 버튼 클릭 시
     * URL 경로: POST /api/payments/{memberId}
     */
    @PostMapping("/{memberId}")
    public ResponseEntity<String> processPayment(@PathVariable Long memberId) {
        try {
            // 서비스의 실제 트랜잭션(결제 확정) 로직을 호출합니다.
            paymentService.processPayment(memberId);

            // 에러 없이 무사히 끝났다면 성공 메시지를 반환합니다.
            return ResponseEntity.ok("결제가 성공적으로 완료되었습니다.");

        } catch (IllegalStateException | IllegalArgumentException e) {
            // 잔고 부족 등 서비스 단계에서 예외(에러)가 터졌을 경우,
            // 400(Bad Request) 상태 코드와 함께 우리가 작성했던 에러 메시지를 프론트엔드에 보냅니다.
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            // 그 외 예상치 못한 서버 에러 처리
            return ResponseEntity.internalServerError().body("결제 처리 중 서버 오류가 발생했습니다.");
        }
    }
}
