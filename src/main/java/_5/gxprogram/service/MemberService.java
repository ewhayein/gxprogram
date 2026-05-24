package _5.gxprogram.service;

import _5.gxprogram.domain.apply;
import _5.gxprogram.domain.member;
import _5.gxprogram.domain.memberStatus;
import _5.gxprogram.dto.LoginRequestDTO;
import _5.gxprogram.dto.MemberSignupRequestDTO;
import _5.gxprogram.dto.MyPageResponseDTO;
import _5.gxprogram.dto.MyPageResponseDTO.ApplyHistoryDTO;
import _5.gxprogram.repository.ApplyRepository;
import _5.gxprogram.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {
    private final MemberRepository memberRepository;
    private final ApplyRepository applyRepository;

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter D_FMT  = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // 회원 가입
    @Transactional
    public Long signup(MemberSignupRequestDTO dto) {
        // 학번 중복 여부
        if (memberRepository.existsByStudentId(dto.getStudentId())) {
            throw new IllegalArgumentException("이미 사용 중인 학번입니다: " + dto.getStudentId());
        }
        member newMember = member.builder()
                .studentId(dto.getStudentId())
                .password(dto.getPassword())
                .name(dto.getName())
                .major(dto.getMajor())
                .familyName(dto.getFamilyName())
                .role(dto.getRole())
                .status(memberStatus.ACTIVE)
                .build();
        return memberRepository.save(newMember).getId();
    }

    // login 하기
    public Long login(LoginRequestDTO dto) {
        member found = memberRepository.findByStudentId(dto.getStudentId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 학번입니다."));
        if (!found.getPassword().equals(dto.getPassword())) {
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

        return MyPageResponseDTO.builder()
                .memberId(m.getId())
                .studentId(m.getStudentId())
                .name(m.getName())
                .major(m.getMajor())
                .role(m.getRole())
                .status(m.getStatus())
                .applyHistories(historyDTOs)
                .build();
    }

    // apply 엔티티 → ApplyHistoryDTO 변환
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
