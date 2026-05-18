package _5.gxprogram.service;

import _5.gxprogram.domain.member;
import _5.gxprogram.dto.LoginRequestDTO;
import _5.gxprogram.dto.MemberSignupRequestDTO;
import _5.gxprogram.dto.MyPageResponseDTO;
import _5.gxprogram.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

}
