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
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApplyService {

    private final CourseRepository courseRepository;
    private final ApplyRepository applyRepository;
    private final MemberRepository memberRepository;
    private final AccountRepository accountRepository;

    //장바구니 담기 (좌석 차감 없음, IN_CART 상태로 저장)
    @Transactional
    public Long applyCourse(Long courseId, Long memberId) {
        course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 강좌입니다. id = " + courseId));

        member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다. id = " + memberId));

        // 동일 강좌 중복 담기 방지 (IN_CART/PENDING/COMPLETED 중 하나라도 있으면 차단)
        List<apply> existing = applyRepository.findByMemberAndStatusIn(member,
                List.of(applyStatus.IN_CART, applyStatus.PENDING_PAYMENT, applyStatus.PAYMENT_COMPLETED));
        boolean alreadyExists = existing.stream()
                .anyMatch(a -> a.getCourse().getId().equals(courseId));
        if (alreadyExists) {
            throw new BusinessException("이미 신청 중이거나 결제한 강좌입니다.");
        }

        // 좌석 차감 없이 IN_CART 로만 저장 (장바구니 단계)
        apply apply = new apply(course, member);
        return applyRepository.save(apply).getId();
    }

    /** 장바구니에서 선택된 항목들을 결제 대기 상태로 일괄 전환
     *  - 좌석 차감 (낙관적 락 동작)
     *  - 기존 PENDING/COMPLETED 강좌 + 함께 선택된 강좌들과의 시간대 중복 검증
     *  - 각 apply 상태 PENDING_PAYMENT + 만료시각 +10분 세팅
     */
    @Transactional
    public void requestPayment(Long memberId, List<Long> applyIds) {
        if (applyIds == null || applyIds.isEmpty()) {
            throw new BusinessException("결제 신청할 강좌를 1개 이상 선택해주세요.");
        }

        member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다. id = " + memberId));

        // 선택한 항목들이 모두 본인의 IN_CART 인지 검증
        List<apply> targets = applyRepository.findByIdInAndMemberIdAndStatus(
                applyIds, memberId, applyStatus.IN_CART);
        if (targets.size() != applyIds.size()) {
            throw new BusinessException("선택한 항목 중 일부가 장바구니에 없거나 다른 사용자의 것입니다.");
        }

        // 시간대 중복 검증: 기존(PENDING/COMPLETED) + 같이 선택된 것들끼리 누적 비교
        List<apply> checkList = new ArrayList<>(applyRepository.findByMemberAndStatusIn(member,
                List.of(applyStatus.PENDING_PAYMENT, applyStatus.PAYMENT_COMPLETED)));

        for (apply target : targets) {
            course tCourse = target.getCourse();
            for (apply other : checkList) {
                course oCourse = other.getCourse();
                if (tCourse.getTargetDate() != null
                        && tCourse.getTargetDate().equals(oCourse.getTargetDate())
                        && tCourse.getStartTime().isBefore(oCourse.getEndTime())
                        && oCourse.getStartTime().isBefore(tCourse.getEndTime())) {
                    throw new BusinessException(
                            "시간이 겹치는 강좌가 있어 결제 신청할 수 없습니다: " + tCourse.getName());
                }
            }
            checkList.add(target);  // 같이 선택된 것들끼리도 비교되도록 누적
        }

        // 좌석 차감 + 상태/만료시각 전환 (낙관적 락은 course.@Version 으로 동작)
        for (apply target : targets) {
            target.getCourse().decreaseSeats();
            target.requestPayment();
        }
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

    /** 장바구니/결제 대기 취소 — IN_CART는 좌석 X, PENDING_PAYMENT는 좌석 복구 */
    @Transactional
    public void cancelReservation(Long applyId) {
        apply apply = applyRepository.findById(applyId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 신청 내역입니다. id=" + applyId));

        applyStatus prev = apply.getStatus();
        apply.cancel();   // 엔티티 내부에서 CANCELLED로 전환 + 상태 검증

        // PENDING_PAYMENT 였다면 좌석을 차지하고 있었으므로 복구
        if (prev == applyStatus.PENDING_PAYMENT) {
            apply.getCourse().increaseSeats();
        }
        // IN_CART 였다면 좌석을 안 차지했으니 복구 불필요
    }
}
