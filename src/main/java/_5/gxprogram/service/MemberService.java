package _5.gxprogram.service;

import _5.gxprogram.domain.apply;
import _5.gxprogram.domain.member;
import _5.gxprogram.domain.memberStatus;
import _5.gxprogram.domain.account;
import _5.gxprogram.dto.LoginRequestDTO;
import _5.gxprogram.dto.MemberSignupRequestDTO;
import _5.gxprogram.dto.MyPageResponseDTO;
import _5.gxprogram.dto.MyPageResponseDTO.ApplyHistoryDTO;
import _5.gxprogram.repository.AccountRepository;
import _5.gxprogram.repository.ApplyRepository;
import _5.gxprogram.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.concurrent.ThreadLocalRandom;

@Service @RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {
    private final MemberRepository memberRepository;
    private final ApplyRepository applyRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter D_FMT  = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // 회원 가입
    @Transactional
    public Long signup(MemberSignupRequestDTO dto) {
        if (memberRepository.existsByStudentId(dto.getStudentId())) {
            throw new IllegalArgumentException("이미 사용 중인 학번입니다: " + dto.getStudentId());
        }
        member newMember = member.builder()
                .studentId(dto.getStudentId())
                .password(passwordEncoder.encode(dto.getPassword()))
                .name(dto.getName())
                .major(dto.getMajor())
                .familyName(dto.getFamilyName())
                .role(dto.getRole())
                .status(memberStatus.ACTIVE)
                .build();
        member saved = memberRepository.save(newMember);

        // 가상 계좌 자동 발급 (시연용 초기 잔액 50만원이나 실제로는 충전 API로 채우는 구조)
        // 가상 계좌 자동 발급 (시연용 초기 잔액 50만원, 실제로는 충전 API로 채우는 구조)
        account virtualAccount = new account(
                saved,
                generateAccountNumber(),    // ⭐ 인자 제거
                500_000
        );
        accountRepository.save(virtualAccount);

        return saved.getId();
    }

    // 10자리 가상 계좌번호 랜덤 생성 (예: 8347521905) -> 추후 중복 계좌에 대한 대책 필요 (개선필요)
    private String generateAccountNumber() {
        // 1,000,000,000에서 9,999,999,999까지의 범위 -> 첫 자리 0 안 오게
        long num = ThreadLocalRandom.current().nextLong(1_000_000_000L, 10_000_000_000L);
        return String.valueOf(num);
    }

    // 로그인 하기
    public Long login(LoginRequestDTO dto) {
        member found = memberRepository.findByStudentId(dto.getStudentId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 학번입니다."));
        if (!passwordEncoder.matches(dto.getPassword(), found.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        if (found.getStatus() != memberStatus.ACTIVE) {
            throw new IllegalStateException("사용할 수 없는 계정입니다. 상태: " + found.getStatus());
        }
        return found.getId();
    }

    // 마이 페이지
    public MyPageResponseDTO getMyPage(Long memberId) {
        member m = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        List<apply> histories = applyRepository.findAllByMemberIdWithCourse(memberId);

        List<ApplyHistoryDTO> historyDTOs = histories.stream()
                .map(this::toApplyHistoryDTO)
                .collect(Collectors.toList());

        Integer balance = accountRepository.findByMemberId(memberId)
                .map(acc -> acc.getBalance())
                .orElse(null);

        return MyPageResponseDTO.builder()
                .memberId(m.getId())
                .studentId(m.getStudentId())
                .name(m.getName())
                .major(m.getMajor())
                .role(m.getRole())
                .status(m.getStatus())
                .accountBalance(balance)
                .applyHistories(historyDTOs)
                .build();
    }

    // apply 엔티티를 ApplyHistoryDTO 변환
    private ApplyHistoryDTO toApplyHistoryDTO(apply a) {
        String courseInfo = String.format("%s %s~%s / %s",
                a.getCourse().getDayOfWeek(),
                a.getCourse().getStartTime(),
                a.getCourse().getEndTime(),
                a.getCourse().getInstructorName());

        return ApplyHistoryDTO.builder()
                .reservationId(a.getId())
                .programName(a.getCourse().getProgram().getName())
                .courseInfo(courseInfo)
                .status(a.getStatus().name())
                .paymentAmount(a.getPaymentAmount())
                .createdAt(a.getCreatedAt() != null ? a.getCreatedAt().format(DT_FMT) : "-")
                .targetDate(a.getTargetDate() != null ? a.getTargetDate().format(D_FMT) : "-")
                .build();
    }
}
