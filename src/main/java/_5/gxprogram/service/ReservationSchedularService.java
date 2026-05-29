package _5.gxprogram.service;

import _5.gxprogram.domain.apply;
import _5.gxprogram.domain.applyStatus;
import _5.gxprogram.repository.ApplyRepository;
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

    @Scheduled(fixedDelay = 60000) // 1분마다 실행
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