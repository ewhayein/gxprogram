package _5.gxprogram.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

import static org.junit.jupiter.api.Assertions.*;

class courseTest {

    @Test
    void decreaseSeats_success() {
        // given: 좌석이 10개인 강좌 생성
        course course = new course("스프링 부트 마스터", 10);

        // when: 좌석 1개 차감
        course.decreaseSeats();

        // then: 9개가 되어야 함
        assertThat(course.getAvailableSeats()).isEqualTo(9);
    }

    @Test
    void decreaseSeats_fail_out_of_seats() {
        // given: 좌석이 0개인 강좌 생성
        course course = new course("인기 강좌", 0);

        // when & then: 차감 시도 시 예외가 터져야 함
        assertThatThrownBy(() -> course.decreaseSeats())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("강좌가 마감되었습니다. 잔여 좌석이 없습니다.");
    }
}