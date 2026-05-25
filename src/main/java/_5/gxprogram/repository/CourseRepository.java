package _5.gxprogram.repository;

import _5.gxprogram.domain.course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/* 강좌 Repository - QueryDSL 동적 검색 */
@Repository
public interface CourseRepository extends JpaRepository<course, Long>, CourseRepositoryCustom {
}
