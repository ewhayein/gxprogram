package _5.gxprogram.service;

import _5.gxprogram.domain.*; // 모든 도메인 import
import _5.gxprogram.repository.*; // 모든 리포지토리 import
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class ApplyServiceTest {

    @Autowired private ApplyService applyService;
    @Autowired private CourseRepository courseRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private ApplyRepository applyRepository;

    // 1. 필드로 선언하여 어디서든 접근 가능하게 만듭니다.
    private course testCourse;

    @BeforeEach
    void setUp() {
        // 2. 클래스 필드에 저장하여 공유합니다.
        testCourse = new course();
        testCourse.setMaxCapacity(10);
        testCourse.setCurrentCapacity(0);
        testCourse.setAvailableSeats(10); // [핵심] 잔여 좌석은 10개로 시작해야 합니다!
        testCourse.setVersion(0L);

        testCourse = courseRepository.save(testCourse);
        courseRepository.flush();
    }

    @AfterEach
    void tearDown() {
        // 3. 자식(Apply)부터 지우고 부모(Member, Course)를 지워야 FK 에러가 안 납니다.
        applyRepository.deleteAll();
        memberRepository.deleteAll();
        courseRepository.deleteAll();
    }

    @Test
    @DisplayName("100명이 동시에 수강신청을 하면 딱 10명만 성공해야 한다.")
    void concurrency_apply_test() throws InterruptedException {
        // given: 100명의 유저 세팅
        member[] members = new member[100];
        for (int i = 0; i < 100; i++) {
            members[i] = new member("user" + i, "password123", "2026" + i, memberRole.EWHA_STUDENT, memberStatus.ACTIVE);
            memberRepository.save(members[i]);
        }

        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        // when: 100명이 동시에 요청
        for (int i = 0; i < threadCount; i++) {
            final Long memberId = members[i].getId();
            final Long courseId = testCourse.getId(); // 클래스 필드를 사용

            executorService.submit(() -> {
                try {
                    applyService.applyCourse(courseId, memberId);
                    successCount.incrementAndGet();
                } catch (OptimisticLockingFailureException e) {
                    failCount.incrementAndGet();
                } catch (Exception e) {
                    System.err.println("실패 원인: " + e.getMessage());
                    e.printStackTrace();
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // then: 결과 검증
        course updatedCourse = courseRepository.findById(testCourse.getId()).orElseThrow();

// 1. 성공 10명 확인 (successCount가 10이어야 함)
        assertEquals(10, successCount.get());

// 2. 좌석 10개가 줄었는지 확인 (availableSeats가 0이어야 함)
        assertEquals(0, updatedCourse.getAvailableSeats());

// 3. 수강생이 10명이 되었는지 확인 (currentCapacity가 10이어야 함)
        assertEquals(10, updatedCourse.getCurrentCapacity());
    }
}