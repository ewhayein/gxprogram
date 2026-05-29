package _5.gxprogram.controller;

import _5.gxprogram.dto.ReservationRequestDTO;
import _5.gxprogram.service.ApplyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ApplyController {
    private final ApplyService applyService;

    @PostMapping
    public ResponseEntity<String> applyForCourse(@Valid @RequestBody ReservationRequestDTO requestDTO) {

        // 수강 신청
        applyService.applyCourse(requestDTO.getCourseId(), requestDTO.getMemberId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body("수강 신청이 접수되어 결제 대기 상태로 전환되었습니다.");
    }

}
