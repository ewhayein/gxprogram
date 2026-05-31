package _5.gxprogram.service;

import _5.gxprogram.domain.*; // 모든 도메인 import
import _5.gxprogram.repository.*; // 모든 리포지토리 import
import _5.gxprogram.support.TestFixtures;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class ApplyServiceTest {

    @Autowired private ApplyService applyService;
    @Autowired private CourseRepository courseRepository;
    @Autowired private ProgramRepository programRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private ApplyRepository applyRepository;

    private course testCourse;

    @BeforeEach
    void setUp() {
        // program 생성 (ApplyRepository 쿼리의 JOIN FETCH c.program 때문에 필요)
        program testProgram = TestFixtures.createProgram(programCategory.HEALTH, "동시성 테스트 프로그램", 50_000);
        programRepository.save(testProgram);

        testCourse = new course();
        testCourse.setProgram(testProgram);    // ⭐ JOIN FETCH 대상이라 필수 연결
        testCourse.setMaxCapacity(10);
        testCourse.setCurrentCapacity(0);
        testCourse.setAvailableSeats(10); // 잔여 좌석 10개로 시작
        testCourse.setVersion(0L);

        testCourse = courseRepository.save(testCourse);
        courseRepository.flush();
    }

    @AfterEach
    void tearDown() {
        // 자식(Apply)부터 지우고 부모(Member, Course, Program)를 지워야 FK 에러가 안 남
        applyRepository.deleteAll();
        memberRepository.deleteAll();
        courseRepository.deleteAll();
        programRepository.deleteAll();
    }

    @Test
    @DisplayName("100명이 동시에 결제 신청을 하면 좌석 차감(낙관적 락)으로 인해 딱 10명만 성공해야 한다.")
    void concurrency_requestPayment_test() throws InterruptedException {
        // given: 100명의 유저
        member[] members = new member[100];
        for (int i = 0; i < 100; i++) {
            members[i] = new member(
                    "user" + i, "password123", "2026" + i,
                    memberRole.EWHA_STUDENT, memberStatus.ACTIVE);
            memberRepository.save(members[i]);
        }

        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        // when: 각 회원이 장바구니 담기 → 즉시 결제 신청 (좌석 차감은 결제 신청 시점에 발생)
        for (int i = 0; i < threadCount; i++) {
            final Long memberId = members[i].getId();
            final Long courseId = testCourse.getId();

            executorService.submit(() -> {
                try {
                    // 1) 장바구니 담기 — 좌석 차감 없음, 모두 성공해야 함
                    Long applyId = applyService.applyCourse(courseId, memberId);

                    // 2) 결제 신청 — 좌석 차감 (낙관적 락 충돌 시 일부만 성공)
                    applyService.requestPayment(memberId, List.of(applyId));

                    successCount.incrementAndGet();
                } catch (OptimisticLockingFailureException e) {
                    failCount.incrementAndGet();
                } catch (Exception e) {
                    System.err.println("실패 원인: " + e.getMessage());
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // then: 결과 검증
        course updatedCourse = courseRepository.findById(testCourse.getId()).orElseThrow();

        // 1. 결제 신청 성공 10명
        assertEquals(10, successCount.get());

        // 2. 좌석 10개가 차감되어 잔여 0
        assertEquals(0, updatedCourse.getAvailableSeats());

        // 3. 현재 수강생 10명
        assertEquals(10, updatedCourse.getCurrentCapacity());
    }
}
