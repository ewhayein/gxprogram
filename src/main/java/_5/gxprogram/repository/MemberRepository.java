package _5.gxprogram.repository;

import _5.gxprogram.domain.member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<member, Long> {
    boolean existsByStudentId(String studentId);
    Optional<member> findByStudentId(String studentId);
}
