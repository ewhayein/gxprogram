package _5.gxprogram.service;

import static org.junit.jupiter.api.Assertions.*;
import _5.gxprogram.domain.course;
import _5.gxprogram.domain.member;
import _5.gxprogram.domain.memberRole;
import _5.gxprogram.domain.memberStatus;
import _5.gxprogram.repository.ApplyRepository;
import _5.gxprogram.repository.CourseRepository;
import _5.gxprogram.repository.MemberRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Disabled; // 이거 임포트

@SpringBootTest // 실제 스프링 부트 환경과 DB를 띄워서 테스트합니다.
class ApplyServiceTest {

    @Autowired private ApplyService applyService;
    @Autowired private CourseRepository courseRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private ApplyRepository applyRepository;

    @AfterEach
    void tearDown() {
        // 테스트가 끝날 때마다 다음 테스트를 위해 DB를 비워줍니다.
        applyRepository.deleteAll();
        memberRepository.deleteAll();
        courseRepository.deleteAll();
    }

    @Test
    @Disabled("결제 로직 구현 후 다시 활성화하여 테스트할 예정")
    @DisplayName("100명이 동시에 수강신청을 하면 딱 10명만 성공하고 90명은 실패해야 한다.")
    void concurrency_apply_test() throws InterruptedException {
        // given: 좌석이 10개인 강좌 1개와, 유저 100명을 DB에 미리 세팅합니다.
        course course = new course("인기 폭발 GX 헬스", 10, 10);
        courseRepository.save(course);

        member[] members = new member[100];
        for (int i = 0; i < 100; i++) {
            members[i] = new member("user" + i, "password123", "2026" + i, memberRole.EWHA_STUDENT, memberStatus.ACTIVE);
            memberRepository.save(members[i]);
        }

        int threadCount = 100; // 100명의 동시 사용자
        // 32개의 스레드가 100개의 작업을 나누어 병렬로 처리하도록 스레드 풀 생성
        ExecutorService executorService = Executors.newFixedThreadPool(32);

        // 100개의 작업이 모두 끝날 때까지 기다려주는 걸쇠(Latch) 역할
        CountDownLatch latch = new CountDownLatch(threadCount);

        // 멀티스레드 환경에서 안전하게 숫자를 세기 위해 AtomicInteger 사용
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        // when: 100명이 동시에 수강 신청 API를 호출하는 상황 시뮬레이션
        for (int i = 0; i < threadCount; i++) {
            final Long memberId = members[i].getId(); // 각기 다른 유저 ID
            final Long courseId = course.getId();

            executorService.submit(() -> {
                try {
                    // 수강 신청 로직 실행
                    applyService.applyCourse(courseId, memberId);
                    successCount.incrementAndGet(); // 성공 시 카운트 증가
                } catch (OptimisticLockingFailureException e) {
                    // 낙관적 락이 발동하여 튕겨져 나온 경우 (정상적인 실패)
                    failCount.incrementAndGet();
                } catch (Exception e) {
                    //에러 원인 파악 위해 콘솔에 출력 코드 추가
                    System.out.println("에러 원인: " + e.getMessage());
                    e.printStackTrace();
                    failCount.incrementAndGet();
                } finally {
                    // 성공하든 실패하든 끝났음을 알림
                    latch.countDown();
                }
            });
        }

        // 메인 스레드는 100개의 작업이 모두 끝날 때까지 여기서 대기합니다.
        latch.await();

        // then: 결과 검증
        course updatedCourse = courseRepository.findById(course.getId()).orElseThrow();

        // 1. 성공한 예약은 정확히 10건이어야 한다.
        assertThat(successCount.get()).isEqualTo(10);

        // 2. 실패한 예약(락 충돌 + 좌석 부족)은 90건이어야 한다.
        assertThat(failCount.get()).isEqualTo(90);

        // 3. 강좌의 남은 좌석은 0개여야 한다.
        assertThat(updatedCourse.getAvailableSeats()).isEqualTo(0);
    }
}