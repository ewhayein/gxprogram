package _5.gxprogram.service;

import _5.gxprogram.dto.ProgramSearchRequestDTO;
import _5.gxprogram.exception.BusinessException;
import _5.gxprogram.repository.CourseRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)

class CourseServiceTest {

    @InjectMocks
    CourseService courseService;

    @Test
    @DisplayName("종료 시간이 시작 시간보다 빠르면 오류가 발생한다.")
    void invalidTimeRange() {
        ProgramSearchRequestDTO condition = new ProgramSearchRequestDTO();

        condition.setStartTimeFrom("18:00");
        condition.setStartTimeTo("09:00");

        assertThatThrownBy(() ->
                courseService.searchCourses(condition)).isInstanceOf(BusinessException.class).hasMessageContaining("종료 시간은 시작 시간보다 빠를 수 없습니다.");
    }
}