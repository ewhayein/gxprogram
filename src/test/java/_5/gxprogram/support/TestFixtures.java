package _5.gxprogram.support;

import _5.gxprogram.domain.account;
import _5.gxprogram.domain.course;
import _5.gxprogram.domain.member;
import _5.gxprogram.domain.memberRole;
import _5.gxprogram.domain.memberStatus;
import _5.gxprogram.domain.program;
import _5.gxprogram.domain.programCategory;
import _5.gxprogram.domain.programStatus;
import org.springframework.beans.BeanUtils;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 통합 테스트용 도메인 픽스처 빌더.
 * - program 엔티티는 setter가 없어 ReflectionTestUtils로 필드 주입한다.
 * - 모든 메서드는 영속화되지 않은 새 인스턴스를 반환하므로, 호출 측에서 repository.save() 해야 한다.
 */
public final class TestFixtures {

    private TestFixtures() {}

    public static member createMember(String name, String studentId) {
        return new member(name, "password!", studentId, memberRole.EWHA_STUDENT, memberStatus.ACTIVE);
    }

    public static account createAccount(member member, int balance) {
        // account의 NoArgsConstructor가 protected라 BeanUtils로 우회 생성
        account a = BeanUtils.instantiateClass(account.class);
        ReflectionTestUtils.setField(a, "member", member);
        ReflectionTestUtils.setField(a, "accountNumber", "1234567890");
        ReflectionTestUtils.setField(a, "balance", balance);
        return a;
    }

    public static program createProgram(programCategory category, String name, int price) {
        program p = BeanUtils.instantiateClass(program.class);
        ReflectionTestUtils.setField(p, "category", category);
        ReflectionTestUtils.setField(p, "name", name);
        ReflectionTestUtils.setField(p, "price", price);
        return p;
    }

    /**
     * 강좌 생성: 잔여좌석 = maxCapacity, 현재인원 = 0
     */
    public static course createCourse(program program,
                                      String name,
                                      int maxCapacity,
                                      LocalDate targetDate,
                                      LocalTime startTime,
                                      LocalTime endTime) {
        course c = new course();
        c.setName(name);
        c.setProgram(program);
        c.setMaxCapacity(maxCapacity);
        c.setAvailableSeats(maxCapacity);
        c.setCurrentCapacity(0);
        c.setTargetDate(targetDate);
        c.setStartTime(startTime);
        c.setEndTime(endTime);
        c.setStatus(programStatus.ACTIVE);
        c.setDayOfWeek("월수금");
        c.setInstructorName("홍길동");
        return c;
    }
}
