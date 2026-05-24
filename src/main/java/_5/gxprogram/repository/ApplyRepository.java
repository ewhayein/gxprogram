package _5.gxprogram.repository;

import _5.gxprogram.domain.apply;
import _5.gxprogram.domain.applyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplyRepository extends JpaRepository<apply, Long> {
    /* 전체 신청 내역 */
    @Query("SELECT a FROM apply a " +
            "JOIN FETCH a.course c " +
            "JOIN FETCH c.program p " +
            "WHERE a.member.id = :memberId " +
            "ORDER BY a.createdAt DESC")
    List<apply> findAllByMemberIdWithCourse(@Param("memberId") Long memberId);

    /* 신청 내역만 조회 */
    @Query("SELECT a FROM apply a " +
            "JOIN FETCH a.course c " +
            "JOIN FETCH c.program p " +
            "WHERE a.member.id = :memberId AND a.status = :status " +
            "ORDER BY a.createdAt DESC")
    List<apply> findByMemberIdAndStatus(@Param("memberId") Long memberId, @Param("status") applyStatus status);
}
