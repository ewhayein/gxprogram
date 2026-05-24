package _5.gxprogram.controller;

import _5.gxprogram.dto.CourseResponseDTO;
import _5.gxprogram.dto.ProgramSearchRequestDTO;
import _5.gxprogram.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {
    private final CourseService courseService;

    /* 강좌 검색 (다중 필터), 모든 파라미터는 선택사항 - null이면 해당 조건 무시 */
    @GetMapping("/search")
    public ResponseEntity<List<CourseResponseDTO>> searchCourses(
            @ModelAttribute ProgramSearchRequestDTO condition) {
        List<CourseResponseDTO> result = courseService.searchCourses(condition);
        return ResponseEntity.ok(result);
    }
}
