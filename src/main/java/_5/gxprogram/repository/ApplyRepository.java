package _5.gxprogram.repository;

import _5.gxprogram.domain.apply;
import _5.gxprogram.domain.applyStatus;
import _5.gxprogram.domain.member;
import _5.gxprogram.domain.programCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;

import java.util.List;

@Repository
public interface ApplyRepository extends JpaRepository<apply, Long> {

    /* 전체 신청 내역 (마이페이지) */
    @Query("SELECT a FROM apply a " +
            "JOIN FETCH a.course c " +
            "JOIN FETCH c.program p " +
            "WHERE a.member.id = :memberId " +
            "ORDER BY a.createdAt DESC")
    List<apply> findAllByMemberIdWithCourse(@Param("memberId") Long memberId);

    /* 특정 회원의 특정 상태 예약 목록 (course/program JOIN FETCH로 N+1 방지) */
    @Query("SELECT a FROM apply a " +
            "JOIN FETCH a.course c " +
            "JOIN FETCH c.program p " +
            "WHERE a.member.id = :memberId AND a.status = :status " +
            "ORDER BY a.createdAt DESC")
    List<apply> findByMemberIdAndStatus(@Param("memberId") Long memberId, @Param("status") applyStatus status);

    /* 결제 정책 검증용: 특정 카테고리·상태의 예약 개수 */
    @Query("SELECT count(a) FROM apply a " +
            "JOIN a.course c " +
            "JOIN c.program p " +
            "WHERE a.member = :member " +
            "AND p.category = :category " +
            "AND a.status = :status")
    long countByMemberAndCategoryAndStatus(@Param("member") member member,
                                           @Param("category") programCategory category,
                                           @Param("status") applyStatus status);

    /* 중복 신청 검증용: 회원의 PENDING/COMPLETED 예약 전체 조회 */
    @Query("SELECT r FROM apply r WHERE r.member = :member AND r.status IN :statuses")
    List<apply> findByMemberAndStatusIn(@Param("member") member member, @Param("statuses") List<applyStatus> statuses);

    List<apply> findByStatusAndExpiresAtBefore(applyStatus status, LocalDateTime now);
}
