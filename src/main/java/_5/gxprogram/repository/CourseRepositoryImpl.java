package _5.gxprogram.repository;

import _5.gxprogram.domain.*;
import _5.gxprogram.dto.CourseResponseDTO;
import _5.gxprogram.dto.ProgramSearchRequestDTO;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.time.LocalTime;
import java.util.List;


@RequiredArgsConstructor
public class CourseRepositoryImpl implements CourseRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    // 다중 필터 검색
    @Override
    public List<CourseResponseDTO> searchCourses(ProgramSearchRequestDTO condition) {
        Qcourse c = Qcourse.course;
        Qprogram p = Qprogram.program;

        return queryFactory
                .select(Projections.constructor(CourseResponseDTO.class,
                        p.id, p.name, p.centerType, p.category,
                        p.price, p.difficulty, p.remarks,
                        c.id, c.instructorName, c.dayOfWeek,
                        c.startTime, c.endTime,
                        c.maxCapacity, c.currentCapacity, c.status))
                .from(c)
                .join(c.program, p)
                .where(
                        centerEq(condition.getCenter()),
                        categoryEq(condition.getCategory()),
                        keywordContains(condition.getKeyword()),
                        dayOfWeekContains(condition.getDayOfWeek()),
                        startTimeBetween(condition.getStartTimeFrom(), condition.getStartTimeTo()),
                        maxPriceLoe(condition.getMaxPrice()),
                        hasAvailableSlot(condition.getHasAvailableSlot()),
                        c.status.eq(programStatus.ACTIVE)
                )
                .orderBy(p.centerType.asc(), p.category.asc(), c.startTime.asc())
                .fetch();
    }

    private BooleanExpression centerEq(centerType center) {
        return center != null ? Qprogram.program.centerType.eq(center) : null;
    }

    private BooleanExpression categoryEq(programCategory category) {
        return category != null ? Qprogram.program.category.eq(category) : null;
    }

    private BooleanExpression keywordContains(String keyword) {
        return (keyword != null && !keyword.isBlank())
                ? Qprogram.program.name.containsIgnoreCase(keyword) : null;
    }

    private BooleanExpression dayOfWeekContains(String day) {
        if (day == null || day.isBlank()) return null;

        // 오류 발생했던 부분: 프론트에서는 쉼표로 보내는데, 여기서 문자열 통째로 LIKE '%월%' 검색하는 문제 (수업시간에 진행했음)
        BooleanExpression result = null;
        for (String d : day.split(",")) {
            String trimmed = d.trim();
            if (trimmed.isEmpty()) continue;
            BooleanExpression cond = Qcourse.course.dayOfWeek.contains(trimmed);
            result = (result == null) ? cond : result.and(cond);
        }
        return result;
    }

    private BooleanExpression startTimeBetween(String from, String to) {
        if (from == null && to == null) return null;
        Qcourse c = Qcourse.course;
        BooleanExpression goe = (from != null) ? c.startTime.goe(LocalTime.parse(from)) : null;
        BooleanExpression loe = (to != null) ? c.startTime.loe(LocalTime.parse(to)) : null;
        if (goe != null && loe != null) return goe.and(loe);
        return goe != null ? goe : loe;
    }

    private BooleanExpression maxPriceLoe(Integer maxPrice) {
        return maxPrice != null ? Qprogram.program.price.loe(maxPrice) : null;
    }

    private BooleanExpression hasAvailableSlot(Boolean flag) {
        if (flag == null || !flag) return null;
        return Qcourse.course.currentCapacity.lt(Qcourse.course.maxCapacity);
    }
}
