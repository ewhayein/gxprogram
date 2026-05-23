package _5.gxprogram.exception;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice // 모든 Controller에서 발생하는 예외를 이곳에서 가로챈다.
public class GlobalExceptionHandler {
    /**
     * 낙관적 락 동시성 충돌 예외 처리
     */
    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLockingFailure(OptimisticLockingFailureException e) {
        // 동시성 충돌이 발생한 것이므로 409 Conflict 상태코드를 반환하는 것이 가장 적절합니다.
        ErrorResponse errorResponse = new ErrorResponse(
                "CONCURRENCY_CONFLICT",
                "동시 접속으로 수강 신청에 실패하였습니다. 다시 시도해 주세요."
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * 비즈니스 로직 검증 실패 처리 (예: 좌석 부족, 이미 취소된 건 환불 불가 등)
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException e) {
        ErrorResponse errorResponse = new ErrorResponse(
                "BAD_REQUEST",
                e.getMessage() // 엔티티에서 throw한 메시지("강좌가 마감되었습니다" 등)를 그대로 전달
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}
