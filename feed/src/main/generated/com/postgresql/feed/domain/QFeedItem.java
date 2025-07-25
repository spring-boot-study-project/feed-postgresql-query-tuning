package com.postgresql.feed.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QFeedItem is a Querydsl query type for FeedItem
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFeedItem extends EntityPathBase<FeedItem> {

    private static final long serialVersionUID = -1573206664L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QFeedItem feedItem = new QFeedItem("feedItem");

    public final com.postgresql.feed.domain.common.QBaseTimeEntity _super = new com.postgresql.feed.domain.common.QBaseTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final DateTimePath<java.time.LocalDateTime> firstHighlightAt = createDateTime("firstHighlightAt", java.time.LocalDateTime.class);

    public final NumberPath<Integer> highlightCount = createNumber("highlightCount", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final DateTimePath<java.time.LocalDateTime> lastHighlightAt = createDateTime("lastHighlightAt", java.time.LocalDateTime.class);

    public final QPage page;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final QUser user;

    public final EnumPath<FeedItem.FeedVisibility> visibility = createEnum("visibility", FeedItem.FeedVisibility.class);

    public QFeedItem(String variable) {
        this(FeedItem.class, forVariable(variable), INITS);
    }

    public QFeedItem(Path<? extends FeedItem> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QFeedItem(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QFeedItem(PathMetadata metadata, PathInits inits) {
        this(FeedItem.class, metadata, inits);
    }

    public QFeedItem(Class<? extends FeedItem> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.page = inits.isInitialized("page") ? new QPage(forProperty("page")) : null;
        this.user = inits.isInitialized("user") ? new QUser(forProperty("user")) : null;
    }

}

