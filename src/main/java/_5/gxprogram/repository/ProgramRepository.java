package _5.gxprogram.repository;

import _5.gxprogram.domain.program;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgramRepository extends JpaRepository<program, Long> {
}
