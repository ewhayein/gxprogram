package _5.gxprogram.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * Qaccount is a Querydsl query type for account
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class Qaccount extends EntityPathBase<account> {

    private static final long serialVersionUID = 627351830L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final Qaccount account = new Qaccount("account");

    public final StringPath accountNumber = createString("accountNumber");

    public final NumberPath<Integer> balance = createNumber("balance", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final Qmember member;

    public Qaccount(String variable) {
        this(account.class, forVariable(variable), INITS);
    }

    public Qaccount(Path<? extends account> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public Qaccount(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public Qaccount(PathMetadata metadata, PathInits inits) {
        this(account.class, metadata, inits);
    }

    public Qaccount(Class<? extends account> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.member = inits.isInitialized("member") ? new Qmember(forProperty("member")) : null;
    }

}

