package _5.gxprogram.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * Qapply is a Querydsl query type for apply
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class Qapply extends EntityPathBase<apply> {

    private static final long serialVersionUID = 622280855L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final Qapply apply = new Qapply("apply");

    public final Qcourse course;

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> expiresAt = createDateTime("expiresAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final Qmember member;

    public final NumberPath<Integer> paymentAmount = createNumber("paymentAmount", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> paymentCompletedAt = createDateTime("paymentCompletedAt", java.time.LocalDateTime.class);

    public final EnumPath<applyStatus> status = createEnum("status", applyStatus.class);

    public final DatePath<java.time.LocalDate> targetDate = createDate("targetDate", java.time.LocalDate.class);

    public Qapply(String variable) {
        this(apply.class, forVariable(variable), INITS);
    }

    public Qapply(Path<? extends apply> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public Qapply(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public Qapply(PathMetadata metadata, PathInits inits) {
        this(apply.class, metadata, inits);
    }

    public Qapply(Class<? extends apply> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.course = inits.isInitialized("course") ? new Qcourse(forProperty("course"), inits.get("course")) : null;
        this.member = inits.isInitialized("member") ? new Qmember(forProperty("member")) : null;
    }

}

