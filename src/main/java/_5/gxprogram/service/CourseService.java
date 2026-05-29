package _5.gxprogram.service;

import _5.gxprogram.dto.CourseResponseDTO;
import _5.gxprogram.dto.ProgramSearchRequestDTO;
import _5.gxprogram.exception.BusinessException;
import _5.gxprogram.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service @RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseService {
    private final CourseRepository courseRepository;

    // 강좌 다중 필터 검색
    public List<CourseResponseDTO> searchCourses(ProgramSearchRequestDTO condition) {
        validateTimeRange(condition);
        return courseRepository.searchCourses(condition);
    }

    private void validateTimeRange(ProgramSearchRequestDTO condition){
        String from = condition.getStartTimeFrom();
        String to = condition.getStartTimeTo();

        if (from == null || from.isBlank() || to == null || to.isBlank()){
            return;
        }
        LocalTime fromTime;
        LocalTime toTime;
        try{
            fromTime = LocalTime.parse(from);
            toTime = LocalTime.parse(to);
        } catch (DateTimeParseException e){
            throw new BusinessException("시간 형식이 올바르지 않습니다.");
        }

        if (fromTime.isAfter(toTime)){
            throw new BusinessException("종료 시간은 시작 시간보다 빠를 수 없습니다.");
        }
    }
}
