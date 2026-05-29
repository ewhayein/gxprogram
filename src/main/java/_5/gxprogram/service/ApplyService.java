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
import _5.gxprogram.repository.MemberRepository;
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

    //수강 신청
    @Transactional
    public Long applyCourse(Long courseId, Long memberId) {
        // 강좌 및 회원 조회
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

        // 강좌 잔여 좌석 차감
        course.decreaseSeats();

        // 예약생성
        apply apply = new apply(course, member);

        // DB에 저장
        courseRepository.save(course);
        apply savedApply = applyRepository.save(apply);

        return savedApply.getId();
    }

    //결제 완료 건에 대한 환불 로직
    @Transactional
    public void refundCourse(Long applyId) {
        //기존 신청내역 조회
        apply apply = applyRepository.findById(applyId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 신청 내역입니다. id=" + applyId));

        // 환불 처리
        apply.refund();

        //좌석 복구
        course course = apply.getCourse();
        course.increaseSeats();
        courseRepository.save(course);

        // 계좌 잔액 환불
        account account = accountRepository.findByMemberId(apply.getMember().getId())
                .orElseThrow(() -> new IllegalArgumentException("회원의 계좌 정보를 찾을 수 없습니다."));
        account.setBalance(account.getBalance() + apply.getPaymentAmount());
    }
}
