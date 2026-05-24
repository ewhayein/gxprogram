package _5.gxprogram.repository;

import _5.gxprogram.domain.apply;
import _5.gxprogram.domain.applyStatus;
import _5.gxprogram.domain.member;
import _5.gxprogram.domain.programCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ApplyRepository extends JpaRepository<apply, Long> {
    // 🔥 특정 회원의 특정 상태(예: PENDING_PAYMENT)인 예약 목록만 가져옵니다.
    List<apply> findByMemberIdAndStatus(Long memberId, applyStatus status);
    long countByMemberAndCategoryAndStatus(member member, programCategory category, applyStatus status);
    @Query("select r from apply r where r.member = :member and r.status in :statuses")
    List<apply> findByMemberAndStatusIn(@Param("member") member member, @Param("statuses") List<applyStatus> statuses);
}
