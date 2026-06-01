package _5.gxprogram.service;

import _5.gxprogram.domain.apply;
import _5.gxprogram.domain.applyStatus;
import _5.gxprogram.repository.ApplyRepository;
import _5.gxprogram.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationSchedularService {

    private final ApplyRepository applyRepository;

    @Scheduled(fixedDelay = 5000) // 5초마다 실행 --> 1분마다 실행하니 타임아웃 직후 새로고침하면 상태 반영이 안 되는 문제로 인함.
    @Transactional
    public void cancelExpiredReservations() {
        List<apply> expiredList = applyRepository.findByStatusAndExpiresAtBefore(
                applyStatus.PENDING_PAYMENT, LocalDateTime.now()
        );
        for (apply reservation : expiredList) {
            reservation.cancel();
            reservation.getCourse().increaseSeats();// 상태 CANCELLED로 변경 + 좌석 복구
        }
    }
}