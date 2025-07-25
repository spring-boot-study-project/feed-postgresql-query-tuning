package com.postgresql.feed.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QHighlight is a Querydsl query type for Highlight
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QHighlight extends EntityPathBase<Highlight> {

    private static final long serialVersionUID = -541879827L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QHighlight highlight = new QHighlight("highlight");

    public final com.postgresql.feed.domain.common.QBaseTimeEntity _super = new com.postgresql.feed.domain.common.QBaseTimeEntity(this);

    public final EnumPath<Highlight.HighlightColor> color = createEnum("color", Highlight.HighlightColor.class);

    public final StringPath contextAfter = createString("contextAfter");

    public final StringPath contextBefore = createString("contextBefore");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Integer> endOffset = createNumber("endOffset", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QPage page;

    public final NumberPath<Integer> startOffset = createNumber("startOffset", Integer.class);

    public final StringPath text = createString("text");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final QUser user;

    public final EnumPath<Highlight.HighlightVisibility> visibility = createEnum("visibility", Highlight.HighlightVisibility.class);

    public QHighlight(String variable) {
        this(Highlight.class, forVariable(variable), INITS);
    }

    public QHighlight(Path<? extends Highlight> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QHighlight(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QHighlight(PathMetadata metadata, PathInits inits) {
        this(Highlight.class, metadata, inits);
    }

    public QHighlight(Class<? extends Highlight> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.page = inits.isInitialized("page") ? new QPage(forProperty("page")) : null;
        this.user = inits.isInitialized("user") ? new QUser(forProperty("user")) : null;
    }

}

