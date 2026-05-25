package _5.gxprogram.repository;

import _5.gxprogram.domain.account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<account, Long> {

    // 🔥 특정 회원의 고유 ID(또는 학번)를 기준으로 계좌 정보를 찾아옵니다.
    Optional<account> findByMemberId(Long memberId);
}
