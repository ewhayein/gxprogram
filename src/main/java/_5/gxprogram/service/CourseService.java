package _5.gxprogram.service;

import _5.gxprogram.dto.CourseResponseDTO;
import _5.gxprogram.dto.ProgramSearchRequestDTO;
import _5.gxprogram.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseService {
    private final CourseRepository courseRepository;

    /* 강좌 다중 필터 검색 */
    public List<CourseResponseDTO> searchCourses(ProgramSearchRequestDTO condition) {
        return courseRepository.searchCourses(condition);
    }
}
