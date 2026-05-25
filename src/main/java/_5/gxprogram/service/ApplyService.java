package _5.gxprogram.service;

import _5.gxprogram.domain.account;
import _5.gxprogram.domain.apply;
import _5.gxprogram.domain.applyStatus;
import _5.gxprogram.domain.course;
import _5.gxprogram.domain.member;
import _5.gxprogram.exception.BusinessException;
import _5.gxprogram.repository.AccountRepository;
import _5.gxprogram.repository.ApplyRepository;
import _5.gxprogram.repository.CourseRepository;
import _5.gxprogram.repository.MemberRepository; // 가상의 회원 리포지토리
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApplyService {

    private final CourseRepository courseRepository;
    private final ApplyRepository applyRepository;
    private final MemberRepository memberRepository;
    private final AccountRepository accountRepository;

    /**
     * 수강 신청 (예약 비즈니스 로직)
     */
    @Transactional // 데이터 변경이 일어나므로 쓰기 트랜잭션을 적용합니다.
    public Long applyCourse(Long courseId, Long memberId) {
        // 1. 강좌 및 회원 조회 (존재하지 않으면 예외 발생)
        course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 강좌입니다. id = " + courseId));

        member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다. id = " + memberId));

        List<apply> existingReservations = applyRepository.findByMemberAndStatusIn(member, List.of(applyStatus.PENDING_PAYMENT, applyStatus.PAYMENT_COMPLETED));

        for (apply res: existingReservations){
            course existingCourse = res.getCourse();

            if (course.getTargetDate().equals(existingCourse.getTargetDate())){
                if(course.getStartTime().isBefore(existingCourse.getEndTime()) &&
                existingCourse.getStartTime().isBefore(course.getEndTime())) {
                    throw new BusinessException("해당 날짜에 이미 다른 강좌가 예약되어 있습니다.");
                }
            }
        }

        // 2. 강좌 잔여 좌석 1개 차감 (내부적으로 수량 검증 로직 작동)
        // 💡 이때 여러 명이 동시에 접근하면 엔티티의 @Version 덕분에 낙관적 락이 작동하여 충돌을 감지합니다.
        course.decreaseSeats();

        // 3. Apply(예약) 엔티티 생성 (초기 상태: PENDING_PAYMENT, 만료시간: +10분 자동 세팅)
        apply apply = new apply(course, member);

        // 4. DB에 저장
        courseRepository.save(course);
        apply savedApply = applyRepository.save(apply);

        return savedApply.getId();
    }

    /**
     * 결제 완료 건에 대한 환불 로직
     */
    @Transactional
    public void refundCourse(Long applyId) {
        // 1. 기존 신청(예약) 내역 조회
        apply apply = applyRepository.findById(applyId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 신청 내역입니다. id=" + applyId));

        // 2. Apply 엔티티의 상태를 REFUNDED로 변경 (내부적으로 = 엔티티에서 PAYMENT_COMPLETED 상태인지 검증)
        apply.refund();

        // 3. 해당 강좌의 좌석을 다시 1개 복구 (내부적으로 최대 정원 초과 검증)
        course course = apply.getCourse();
        course.increaseSeats();
        courseRepository.save(course);

        // 4. 회원 계좌 잔액 환불 (결제 시 기록된 paymentAmount만큼 복구)
        account account = accountRepository.findByMemberId(apply.getMember().getId())
                .orElseThrow(() -> new IllegalArgumentException("회원의 계좌 정보를 찾을 수 없습니다."));
        account.setBalance(account.getBalance() + apply.getPaymentAmount());
    }
}
