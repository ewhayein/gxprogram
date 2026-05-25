package _5.gxprogram.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * Qcourse is a Querydsl query type for course
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class Qcourse extends EntityPathBase<course> {

    private static final long serialVersionUID = -2127640558L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final Qcourse course = new Qcourse("course");

    public final NumberPath<Integer> availableSeats = createNumber("availableSeats", Integer.class);

    public final NumberPath<Integer> currentCapacity = createNumber("currentCapacity", Integer.class);

    public final StringPath dayOfWeek = createString("dayOfWeek");

    public final TimePath<java.time.LocalTime> endTime = createTime("endTime", java.time.LocalTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath instructorName = createString("instructorName");

    public final NumberPath<Integer> maxCapacity = createNumber("maxCapacity", Integer.class);

    public final StringPath name = createString("name");

    public final Qprogram program;

    public final TimePath<java.time.LocalTime> startTime = createTime("startTime", java.time.LocalTime.class);

    public final EnumPath<programStatus> status = createEnum("status", programStatus.class);

    public final DatePath<java.time.LocalDate> targetDate = createDate("targetDate", java.time.LocalDate.class);

    public final NumberPath<Long> version = createNumber("version", Long.class);

    public Qcourse(String variable) {
        this(course.class, forVariable(variable), INITS);
    }

    public Qcourse(Path<? extends course> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public Qcourse(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public Qcourse(PathMetadata metadata, PathInits inits) {
        this(course.class, metadata, inits);
    }

    public Qcourse(Class<? extends course> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.program = inits.isInitialized("program") ? new Qprogram(forProperty("program")) : null;
    }

}

