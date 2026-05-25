package _5.gxprogram.integration;

import _5.gxprogram.domain.account;
import _5.gxprogram.domain.apply;
import _5.gxprogram.domain.applyStatus;
import _5.gxprogram.domain.course;
import _5.gxprogram.domain.member;
import _5.gxprogram.domain.program;
import _5.gxprogram.domain.programCategory;
import _5.gxprogram.dto.OrderSummaryResponseDTO;
import _5.gxprogram.dto.ReservationRequestDTO;
import _5.gxprogram.exception.BusinessException;
import _5.gxprogram.repository.AccountRepository;
import _5.gxprogram.repository.ApplyRepository;
import _5.gxprogram.repository.CourseRepository;
import _5.gxprogram.repository.MemberRepository;
import _5.gxprogram.repository.ProgramRepository;
import _5.gxprogram.service.ApplyService;
import _5.gxprogram.service.PaymentService;
import _5.gxprogram.support.TestFixtures;
import tools.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 통합 테스트 — 수강 신청 / 결제 / 환불 전 흐름.
 * 시나리오 S1~S7 (서비스 레벨) + S9 (MockMvc 컨트롤러 레벨)을 @Nested로 그룹화.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PaymentFlowIntegrationTest {

    @Autowired ApplyService applyService;
    @Autowired PaymentService paymentService;
    @Autowired MemberRepository memberRepository;
    @Autowired AccountRepository accountRepository;
    @Autowired ProgramRepository programRepository;
    @Autowired CourseRepository courseRepository;
    @Autowired ApplyRepository applyRepository;
    @PersistenceContext EntityManager em;

    @Test
    @DisplayName("부트업: 스프링 컨텍스트가 로드되고 필요한 빈들이 주입된다")
    void contextLoads() {
        assertThat(applyService).isNotNull();
        assertThat(paymentService).isNotNull();
        assertThat(memberRepository).isNotNull();
        assertThat(accountRepository).isNotNull();
    }

    @Test
    @DisplayName("부트업: TestFixtures로 만든 엔티티들이 H2에 영속화된다")
    void fixturesPersistSuccessfully() {
        // given
        member m = memberRepository.save(TestFixtures.createMember("테스트유저", "2300001"));
        accountRepository.save(TestFixtures.createAccount(m, 100_000));
        program p = programRepository.save(TestFixtures.createProgram(programCategory.HEALTH, "헬스 1개월", 50_000));
        course c = courseRepository.save(TestFixtures.createCourse(
                p, "헬스 1개월 - A반", 10,
                LocalDate.of(2026, 6, 1), LocalTime.of(10, 0), LocalTime.of(11, 0)));

        em.flush();
        em.clear();

        // then
        assertThat(courseRepository.findById(c.getId())).isPresent();
        assertThat(accountRepository.findByMemberId(m.getId())).isPresent()
                .get().extracting(account::getBalance).isEqualTo(100_000);
    }

    // ============================================================
    // S1~S7: 서비스 레벨 통합 시나리오 (다음 단계에서 작성)
    // ============================================================

    @Nested
    @DisplayName("S1~S7: 수강 신청 / 결제 / 환불 시나리오")
    class ServiceLayerScenarios {

        @Test
        @DisplayName("S1: 단일 강좌 신청 → 가계산 → 결제 완료 - 좌석/잔액/paymentAmount가 모두 정상 반영된다")
        void s1_singleApplyAndPayment() {
            // ---------- given ----------
            member m = memberRepository.save(TestFixtures.createMember("S1유저", "2300001"));
            accountRepository.save(TestFixtures.createAccount(m, 100_000));
            program p = programRepository.save(
                    TestFixtures.createProgram(programCategory.HEALTH, "헬스 1개월", 50_000));
            course c = courseRepository.save(TestFixtures.createCourse(
                    p, "헬스 1개월 - A반", 10,
                    LocalDate.of(2026, 6, 1), LocalTime.of(10, 0), LocalTime.of(11, 0)));
            em.flush();
            em.clear();

            // ---------- when 1: 수강 신청 ----------
            Long applyId = applyService.applyCourse(c.getId(), m.getId());
            em.flush();
            em.clear();

            // ---------- then 1: PENDING_PAYMENT 생성, 좌석 1개 차감 ----------
            apply pending = applyRepository.findById(applyId).orElseThrow();
            assertThat(pending.getStatus()).isEqualTo(applyStatus.PENDING_PAYMENT);
            assertThat(pending.getExpiresAt()).isNotNull();

            course afterApply = courseRepository.findById(c.getId()).orElseThrow();
            assertThat(afterApply.getAvailableSeats()).isEqualTo(9);
            assertThat(afterApply.getCurrentCapacity()).isEqualTo(1);

            // ---------- when 2: 결제 가계산 (영수증 조회) ----------
            OrderSummaryResponseDTO summary = paymentService.getOrderSummary(m.getId());

            // ---------- then 2: 단건이라 할인 0원, 청구액 = 원가 ----------
            assertThat(summary.getTotalOriginalPrice()).isEqualTo(50_000);
            assertThat(summary.getDiscountAmount()).isEqualTo(0);
            assertThat(summary.getFinalBillingPrice()).isEqualTo(50_000);

            // ---------- when 3: 결제 처리 ----------
            paymentService.processPayment(m.getId());
            em.flush();
            em.clear();

            // ---------- then 3: 결제 완료 + paymentAmount 기록 + 잔액 차감 ----------
            apply completed = applyRepository.findById(applyId).orElseThrow();
            assertThat(completed.getStatus()).isEqualTo(applyStatus.PAYMENT_COMPLETED);
            assertThat(completed.getPaymentAmount()).isEqualTo(50_000);
            assertThat(completed.getPaymentCompletedAt()).isNotNull();

            account afterPay = accountRepository.findByMemberId(m.getId()).orElseThrow();
            assertThat(afterPay.getBalance()).isEqualTo(50_000);
        }

        @Test
        @DisplayName("S2: 헬스 + GX 콤보 결제 → 헬스 가격의 20% 할인이 헬스 apply에만 분배된다")
        void s2_comboDiscount() {
            // given
            member m = memberRepository.save(TestFixtures.createMember("S2유저", "2300002"));
            accountRepository.save(TestFixtures.createAccount(m, 500_000));
            program health = programRepository.save(
                    TestFixtures.createProgram(programCategory.HEALTH, "헬스 1개월", 100_000));
            program gx = programRepository.save(
                    TestFixtures.createProgram(programCategory.GX, "GX 주3회", 50_000));
            course chealth = courseRepository.save(TestFixtures.createCourse(
                    health, "헬스 A", 10,
                    LocalDate.of(2026, 6, 1), LocalTime.of(10, 0), LocalTime.of(11, 0)));
            course cgx = courseRepository.save(TestFixtures.createCourse(
                    gx, "GX A", 10,
                    LocalDate.of(2026, 6, 2), LocalTime.of(10, 0), LocalTime.of(11, 0)));
            em.flush();
            em.clear();

            // when: 신청 2건
            Long aHealthId = applyService.applyCourse(chealth.getId(), m.getId());
            Long aGxId = applyService.applyCourse(cgx.getId(), m.getId());
            em.flush();
            em.clear();

            // then: 영수증 검증
            OrderSummaryResponseDTO summary = paymentService.getOrderSummary(m.getId());
            assertThat(summary.getTotalOriginalPrice()).isEqualTo(150_000);
            assertThat(summary.getDiscountAmount()).isEqualTo(20_000); // 헬스 100,000 * 20%
            assertThat(summary.getFinalBillingPrice()).isEqualTo(130_000);

            // when: 결제 처리
            paymentService.processPayment(m.getId());
            em.flush();
            em.clear();

            // then: 각 apply의 paymentAmount 분배 확인
            apply ahealth = applyRepository.findById(aHealthId).orElseThrow();
            apply agx = applyRepository.findById(aGxId).orElseThrow();
            assertThat(ahealth.getPaymentAmount()).isEqualTo(80_000); // 100,000 - 20,000
            assertThat(agx.getPaymentAmount()).isEqualTo(50_000);     // 할인 없음

            account a = accountRepository.findByMemberId(m.getId()).orElseThrow();
            assertThat(a.getBalance()).isEqualTo(370_000); // 500,000 - 130,000
        }

        @Test
        @DisplayName("S3: 수량 할인 대상(GX/TENNIS 등) 3개 결제 → 각 apply에 가격의 10% 분배")
        void s3_quantityDiscount() {
            // given
            member m = memberRepository.save(TestFixtures.createMember("S3유저", "2300003"));
            accountRepository.save(TestFixtures.createAccount(m, 500_000));
            program gx = programRepository.save(
                    TestFixtures.createProgram(programCategory.GX, "GX 주3회", 50_000));
            course c1 = courseRepository.save(TestFixtures.createCourse(
                    gx, "GX A", 10,
                    LocalDate.of(2026, 6, 1), LocalTime.of(10, 0), LocalTime.of(11, 0)));
            course c2 = courseRepository.save(TestFixtures.createCourse(
                    gx, "GX B", 10,
                    LocalDate.of(2026, 6, 2), LocalTime.of(10, 0), LocalTime.of(11, 0)));
            course c3 = courseRepository.save(TestFixtures.createCourse(
                    gx, "GX C", 10,
                    LocalDate.of(2026, 6, 3), LocalTime.of(10, 0), LocalTime.of(11, 0)));
            em.flush();
            em.clear();

            // when: 3건 신청
            Long a1 = applyService.applyCourse(c1.getId(), m.getId());
            Long a2 = applyService.applyCourse(c2.getId(), m.getId());
            Long a3 = applyService.applyCourse(c3.getId(), m.getId());
            em.flush();
            em.clear();

            // then: 영수증 - 총 150,000원의 10% 할인
            OrderSummaryResponseDTO summary = paymentService.getOrderSummary(m.getId());
            assertThat(summary.getTotalOriginalPrice()).isEqualTo(150_000);
            assertThat(summary.getDiscountAmount()).isEqualTo(15_000);
            assertThat(summary.getFinalBillingPrice()).isEqualTo(135_000);

            // when: 결제 처리
            paymentService.processPayment(m.getId());
            em.flush();
            em.clear();

            // then: 각 apply에 5,000원씩 할인 분배 (paymentAmount = 45,000)
            assertThat(applyRepository.findById(a1).orElseThrow().getPaymentAmount()).isEqualTo(45_000);
            assertThat(applyRepository.findById(a2).orElseThrow().getPaymentAmount()).isEqualTo(45_000);
            assertThat(applyRepository.findById(a3).orElseThrow().getPaymentAmount()).isEqualTo(45_000);

            account a = accountRepository.findByMemberId(m.getId()).orElseThrow();
            assertThat(a.getBalance()).isEqualTo(365_000); // 500,000 - 135,000
        }

        @Test
        @DisplayName("S4: 잔고 부족 → IllegalStateException + 잔액/apply 상태 변경 없음")
        void s4_insufficientBalance() {
            // given: 잔액 10,000원, 50,000원 강좌 신청
            member m = memberRepository.save(TestFixtures.createMember("S4유저", "2300004"));
            accountRepository.save(TestFixtures.createAccount(m, 10_000));
            program p = programRepository.save(
                    TestFixtures.createProgram(programCategory.HEALTH, "헬스", 50_000));
            course c = courseRepository.save(TestFixtures.createCourse(
                    p, "헬스 A", 10,
                    LocalDate.of(2026, 6, 1), LocalTime.of(10, 0), LocalTime.of(11, 0)));
            em.flush();
            em.clear();

            Long applyId = applyService.applyCourse(c.getId(), m.getId());
            em.flush();
            em.clear();

            // when + then: 결제 시도 → 예외
            assertThatThrownBy(() -> paymentService.processPayment(m.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("잔고가 부족");

            em.clear();

            // then: apply 상태 그대로, 잔액 그대로
            apply unchanged = applyRepository.findById(applyId).orElseThrow();
            assertThat(unchanged.getStatus()).isEqualTo(applyStatus.PENDING_PAYMENT);
            assertThat(unchanged.getPaymentAmount()).isEqualTo(0); // 아직 결제 안 됨

            account a = accountRepository.findByMemberId(m.getId()).orElseThrow();
            assertThat(a.getBalance()).isEqualTo(10_000);
        }

        @Test
        @DisplayName("S5: 같은 날짜·시간 겹치는 강좌 중복 신청 → BusinessException")
        void s5_duplicateTimeSlot() {
            // given: 같은 날, 10:00~11:00 / 10:30~11:30 강좌 2개
            member m = memberRepository.save(TestFixtures.createMember("S5유저", "2300005"));
            accountRepository.save(TestFixtures.createAccount(m, 100_000));
            program p = programRepository.save(
                    TestFixtures.createProgram(programCategory.GX, "GX", 50_000));
            LocalDate date = LocalDate.of(2026, 6, 1);
            course c1 = courseRepository.save(TestFixtures.createCourse(
                    p, "GX 오전 A", 10, date, LocalTime.of(10, 0), LocalTime.of(11, 0)));
            course c2 = courseRepository.save(TestFixtures.createCourse(
                    p, "GX 오전 B", 10, date, LocalTime.of(10, 30), LocalTime.of(11, 30)));
            em.flush();
            em.clear();

            // when 1: 첫 강좌 신청 성공
            applyService.applyCourse(c1.getId(), m.getId());
            em.flush();
            em.clear();

            // when 2: 겹치는 두 번째 신청 → 예외
            assertThatThrownBy(() -> applyService.applyCourse(c2.getId(), m.getId()))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("이미 다른 강좌가 예약");
        }

        @Test
        @DisplayName("S6: 잔여 좌석 0인 강좌 신청 → IllegalStateException")
        void s6_outOfSeats() {
            // given: 좌석 0인 강좌
            member m = memberRepository.save(TestFixtures.createMember("S6유저", "2300006"));
            accountRepository.save(TestFixtures.createAccount(m, 100_000));
            program p = programRepository.save(
                    TestFixtures.createProgram(programCategory.HEALTH, "헬스", 50_000));
            course c = courseRepository.save(TestFixtures.createCourse(
                    p, "마감 강좌", 0,
                    LocalDate.of(2026, 6, 1), LocalTime.of(10, 0), LocalTime.of(11, 0)));
            em.flush();
            em.clear();

            // when + then
            assertThatThrownBy(() -> applyService.applyCourse(c.getId(), m.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("강좌가 마감");
        }

        @Test
        @DisplayName("S7: 결제 완료 후 환불 → 상태 REFUNDED + 좌석 복구 + 잔액 복구")
        void s7_refundAfterPayment() {
            // given
            member m = memberRepository.save(TestFixtures.createMember("S7유저", "2300007"));
            accountRepository.save(TestFixtures.createAccount(m, 100_000));
            program p = programRepository.save(
                    TestFixtures.createProgram(programCategory.HEALTH, "헬스", 50_000));
            course c = courseRepository.save(TestFixtures.createCourse(
                    p, "헬스 A", 10,
                    LocalDate.of(2026, 6, 1), LocalTime.of(10, 0), LocalTime.of(11, 0)));
            em.flush();
            em.clear();

            Long applyId = applyService.applyCourse(c.getId(), m.getId());
            paymentService.processPayment(m.getId());
            em.flush();
            em.clear();

            // sanity check: 결제 완료 상태
            assertThat(applyRepository.findById(applyId).orElseThrow().getStatus())
                    .isEqualTo(applyStatus.PAYMENT_COMPLETED);
            assertThat(accountRepository.findByMemberId(m.getId()).orElseThrow().getBalance())
                    .isEqualTo(50_000);

            // when: 환불
            applyService.refundCourse(applyId);
            em.flush();
            em.clear();

            // then: 상태 REFUNDED, 좌석 복구, 잔액 복구
            apply refunded = applyRepository.findById(applyId).orElseThrow();
            assertThat(refunded.getStatus()).isEqualTo(applyStatus.REFUNDED);

            course afterRefund = courseRepository.findById(c.getId()).orElseThrow();
            assertThat(afterRefund.getAvailableSeats()).isEqualTo(10);
            assertThat(afterRefund.getCurrentCapacity()).isEqualTo(0);

            account a = accountRepository.findByMemberId(m.getId()).orElseThrow();
            assertThat(a.getBalance()).isEqualTo(100_000); // 50,000 환불받아 원상복구
        }
    }

    // ============================================================
    // S9: MockMvc 컨트롤러 레벨 (3개 핵심 케이스)
    // ============================================================

    @Nested
    @DisplayName("S9: 컨트롤러 레벨 (MockMvc)")
    class ControllerLayer {

        @Autowired MockMvc mockMvc;
        @Autowired ObjectMapper objectMapper;

        @Test
        @DisplayName("S9-1: POST /api/reservations 성공 → 201 Created + DB 좌석 차감")
        void s9_1_postReservation_success() throws Exception {
            // given
            member m = memberRepository.save(TestFixtures.createMember("S9유저1", "2300091"));
            accountRepository.save(TestFixtures.createAccount(m, 100_000));
            program p = programRepository.save(
                    TestFixtures.createProgram(programCategory.HEALTH, "헬스", 50_000));
            course c = courseRepository.save(TestFixtures.createCourse(
                    p, "헬스 A", 10,
                    LocalDate.of(2026, 6, 1), LocalTime.of(10, 0), LocalTime.of(11, 0)));
            em.flush();
            em.clear();

            ReservationRequestDTO dto = new ReservationRequestDTO();
            dto.setMemberId(m.getId());
            dto.setCourseId(c.getId());

            // when + then: 201 응답
            mockMvc.perform(post("/api/reservations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("UTF-8")
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isCreated());

            em.flush();  // 1차 캐시 변경사항을 DB로 반영
            em.clear();

            // then: DB 좌석 1개 차감 (Controller가 인자 순서를 올바르게 전달했음을 검증)
            course afterApply = courseRepository.findById(c.getId()).orElseThrow();
            assertThat(afterApply.getAvailableSeats()).isEqualTo(9);
            assertThat(afterApply.getCurrentCapacity()).isEqualTo(1);
        }

        @Test
        @DisplayName("S9-2: POST /api/payments/{memberId} 잔고 부족 → 400 BadRequest + 에러 메시지")
        void s9_2_postPayment_insufficientBalance() throws Exception {
            // given: 잔액 부족
            member m = memberRepository.save(TestFixtures.createMember("S9유저2", "2300092"));
            accountRepository.save(TestFixtures.createAccount(m, 1_000));
            program p = programRepository.save(
                    TestFixtures.createProgram(programCategory.HEALTH, "헬스", 50_000));
            course c = courseRepository.save(TestFixtures.createCourse(
                    p, "헬스 A", 10,
                    LocalDate.of(2026, 6, 1), LocalTime.of(10, 0), LocalTime.of(11, 0)));
            em.flush();
            em.clear();

            applyService.applyCourse(c.getId(), m.getId());
            em.flush();
            em.clear();

            // when + then: 400 응답
            mockMvc.perform(post("/api/payments/{memberId}", m.getId())
                            .characterEncoding("UTF-8"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("잔고가 부족")));
        }

        @Test
        @DisplayName("S9-3: POST /api/reservations - memberId 누락 시 @Valid 검증 작동 → 400")
        void s9_3_postReservation_validationFail() throws Exception {
            // given: memberId 누락 JSON
            String invalidBody = "{\"courseId\": 1}";

            // when + then: 400 응답
            mockMvc.perform(post("/api/reservations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("UTF-8")
                            .content(invalidBody))
                    .andExpect(status().isBadRequest());
        }
    }
}
