package _5.gxprogram.repository;

import _5.gxprogram.dto.CourseResponseDTO;
import _5.gxprogram.dto.ProgramSearchRequestDTO;

import java.util.List;

// QueryDSL 커스텀 검색 메서드 인터페이스
public interface CourseRepositoryCustom {
    List<CourseResponseDTO> searchCourses(ProgramSearchRequestDTO condition);
}
