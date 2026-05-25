package _5.gxprogram.service;

import _5.gxprogram.domain.course;
import _5.gxprogram.domain.program;
import _5.gxprogram.domain.apply;
import _5.gxprogram.domain.programCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class QuantityDiscountPolicyTest {
    QuantityDiscountPolicy discountPolicy = new QuantityDiscountPolicy();
    //테스트용 장바구니 생성 메소드
    private apply createMockReservation(programCategory category, int price) {
        // 1. 가짜(Mock) 객체들 생성
        apply mockReservation = mock(apply.class);
        course mockCourse = mock(course.class);
        program mockProgram = mock(program.class);

        // 2. 가짜 객체들이 어떻게 행동할지 조작 (예: getCourse()를 부르면 가짜 Course를 줘라!)
        when(mockReservation.getCourse()).thenReturn(mockCourse);
        when(mockCourse.getProgram()).thenReturn(mockProgram);
        when(mockProgram.getCategory()).thenReturn(category);
        when(mockProgram.getPrice()).thenReturn(price);

        return mockReservation;
    }

    @Test
    @DisplayName("케이스 A: 헬스 + GX 복합 결제 시 헬스 가격의 20%만 정확히 할인되는가?")
    void testComboDiscount() {
        // Given: 장바구니에 헬스(10만 원)와 GX(5만 원)를 담음
        List<apply> cart = new ArrayList<>();
        cart.add(createMockReservation(programCategory.HEALTH, 100000));
        cart.add(createMockReservation(programCategory.GX, 50000));

        // When: 할인 금액 계산
        int discount = discountPolicy.calculateDiscountAmount(cart);

        // Then: 헬스 가격(10만원)의 20%인 '2만 원'만 할인되어야 함! (GX 10% 할인이 먹히면 안 됨)
        assertEquals(20000, discount);
    }

    @Test
    @DisplayName("케이스 B: 헬스 없이 GX 및 개별 스포츠 3개 결제 시 총액의 10%가 할인되는가?")
    void testQuantityDiscountThreeOrMore() {
        // Given: 장바구니에 GX 2개, 테니스 1개 담음 (총 15만 원)
        List<apply> cart = new ArrayList<>();
        cart.add(createMockReservation(programCategory.GX, 50000));
        cart.add(createMockReservation(programCategory.GX, 50000));
        // 🔥 SPORTS 대신 실제 존재하는 TENNIS를 사용!
        cart.add(createMockReservation(programCategory.TENNIS, 50000));

        // When: 할인 금액 계산
        int discount = discountPolicy.calculateDiscountAmount(cart);

        // Then: 15만 원의 10%인 '1만 5천 원'이 할인되어야 함
        assertEquals(15000, discount);
    }

    @Test
    @DisplayName("케이스 C: 헬스 없이 GX 및 개별 스포츠 2개 결제 시 총액의 5%가 할인되는가?")
    void testQuantityDiscountTwo() {
        // Given: 장바구니에 GX 1개, 골프 1개 담음 (총 10만 원)
        List<apply> cart = new ArrayList<>();
        cart.add(createMockReservation(programCategory.GX, 60000));
        // 🔥 SPORTS 대신 실제 존재하는 GOLF를 사용!
        cart.add(createMockReservation(programCategory.GOLF, 40000));

        // When: 할인 금액 계산
        int discount = discountPolicy.calculateDiscountAmount(cart);

        // Then: 10만 원의 5%인 '5천 원'이 할인되어야 함
        assertEquals(5000, discount);
    }

    @Test
    @DisplayName("케이스 D: 프로그램 1개만 단건 결제 시 할인이 0원인가?")
    void testNoDiscount() {
        // Given: 장바구니에 헬스 1개만 담음 (10만 원)
        List<apply> cart = new ArrayList<>();
        cart.add(createMockReservation(programCategory.HEALTH, 100000));

        // When: 할인 금액 계산
        int discount = discountPolicy.calculateDiscountAmount(cart);

        // Then: 수량/콤보 조건 미달이므로 할인은 '0원'이어야 함
        assertEquals(0, discount);
    }

    @Test
    @DisplayName("케이스 E: SMALL_GROUP은 헬스와 결제 시 콤보 할인이 발동되는가?")
    void testSmallGroupComboDiscount() {
        // Given: 헬스(10만원) + 소그룹(5만원)
        List<apply> cart = new ArrayList<>();
        cart.add(createMockReservation(programCategory.HEALTH, 100000));
        cart.add(createMockReservation(programCategory.SMALL_GROUP, 50000));

        // When
        int discount = discountPolicy.calculateDiscountAmount(cart);

        // Then: 헬스의 20%인 2만원 할인
        assertEquals(20000, discount);
    }

    @Test
    @DisplayName("케이스 F: ONE_TIME_PASS나 MEASUREMENT는 아무리 많이 사도 할인이 안 되는가?")
    void testExcludedCategories() {
        // Given: 일일권 3개(총 3만원), 체력측정 2개(총 2만원) -> 합이 5개!
        List<apply> cart = new ArrayList<>();
        cart.add(createMockReservation(programCategory.ONE_TIME_PASS, 10000));
        cart.add(createMockReservation(programCategory.ONE_TIME_PASS, 10000));
        cart.add(createMockReservation(programCategory.ONE_TIME_PASS, 10000));
        cart.add(createMockReservation(programCategory.MEASUREMENT, 10000));
        cart.add(createMockReservation(programCategory.MEASUREMENT, 10000));

        // When
        int discount = discountPolicy.calculateDiscountAmount(cart);

        // Then: 개수가 5개라도 할인 대상 카테고리가 아니므로 무조건 0원이어야 함!
        assertEquals(0, discount);
    }

    // ===== calculateDiscountPerReservation: 예약별 할인 분배 검증 =====

    @Test
    @DisplayName("[분배] 헬스 + GX 콤보 시: 헬스 apply에만 20% 할인이 분배되고 GX는 분배되지 않는가?")
    void testDistributeComboDiscount() {
        // Given
        apply health = createMockReservation(programCategory.HEALTH, 100000);
        apply gx = createMockReservation(programCategory.GX, 50000);
        List<apply> cart = List.of(health, gx);

        // When
        Map<apply, Integer> distribution = discountPolicy.calculateDiscountPerReservation(cart);

        // Then: 헬스에만 2만원 분배, GX는 맵에 없음
        assertEquals(20000, distribution.getOrDefault(health, 0));
        assertEquals(0, distribution.getOrDefault(gx, 0));
        // 합계는 기존 calculateDiscountAmount와 일치해야 함
        assertEquals(20000, discountPolicy.calculateDiscountAmount(cart));
    }

    @Test
    @DisplayName("[분배] 수량 할인 대상 3개 결제 시: 각 apply에 자신의 가격 * 10%가 분배되는가?")
    void testDistributeQuantityDiscount() {
        // Given: GX(5만), GX(5만), TENNIS(5만)
        apply gx1 = createMockReservation(programCategory.GX, 50000);
        apply gx2 = createMockReservation(programCategory.GX, 50000);
        apply tennis = createMockReservation(programCategory.TENNIS, 50000);
        List<apply> cart = List.of(gx1, gx2, tennis);

        // When
        Map<apply, Integer> distribution = discountPolicy.calculateDiscountPerReservation(cart);

        // Then: 각 5,000원씩 분배, 합계 15,000원
        assertEquals(5000, distribution.getOrDefault(gx1, 0));
        assertEquals(5000, distribution.getOrDefault(gx2, 0));
        assertEquals(5000, distribution.getOrDefault(tennis, 0));
        assertEquals(15000, discountPolicy.calculateDiscountAmount(cart));
    }
}