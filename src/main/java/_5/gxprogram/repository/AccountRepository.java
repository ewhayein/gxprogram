package _5.gxprogram.repository;

import _5.gxprogram.domain.account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<account, Long> {
    Optional<account> findByMemberId(Long memberId);
}
