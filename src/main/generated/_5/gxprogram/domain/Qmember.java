package _5.gxprogram.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * Qmember is a Querydsl query type for member
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class Qmember extends EntityPathBase<member> {

    private static final long serialVersionUID = -1850838383L;

    public static final Qmember member = new Qmember("member1");

    public final StringPath familyName = createString("familyName");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath major = createString("major");

    public final StringPath name = createString("name");

    public final StringPath password = createString("password");

    public final EnumPath<memberRole> role = createEnum("role", memberRole.class);

    public final EnumPath<memberStatus> status = createEnum("status", memberStatus.class);

    public final StringPath studentId = createString("studentId");

    public Qmember(String variable) {
        super(member.class, forVariable(variable));
    }

    public Qmember(Path<? extends member> path) {
        super(path.getType(), path.getMetadata());
    }

    public Qmember(PathMetadata metadata) {
        super(member.class, metadata);
    }

}

