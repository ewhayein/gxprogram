package _5.gxprogram.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * Qprogram is a Querydsl query type for program
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class Qprogram extends EntityPathBase<program> {

    private static final long serialVersionUID = 1495283053L;

    public static final Qprogram program = new Qprogram("program");

    public final EnumPath<programCategory> category = createEnum("category", programCategory.class);

    public final EnumPath<centerType> centerType = createEnum("centerType", centerType.class);

    public final ListPath<course, Qcourse> courses = this.<course, Qcourse>createList("courses", course.class, Qcourse.class, PathInits.DIRECT2);

    public final StringPath difficulty = createString("difficulty");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final NumberPath<Integer> price = createNumber("price", Integer.class);

    public final StringPath remarks = createString("remarks");

    public Qprogram(String variable) {
        super(program.class, forVariable(variable));
    }

    public Qprogram(Path<? extends program> path) {
        super(path.getType(), path.getMetadata());
    }

    public Qprogram(PathMetadata metadata) {
        super(program.class, metadata);
    }

}

