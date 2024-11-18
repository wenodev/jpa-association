package persistence.sql.entity;

import java.util.Map;
import java.util.Optional;

public interface PersistenceContext {
    Optional<Object> getEntity(final CacheKey key);

    void managedEntity(final CacheKey key, final Object entity);

    void removeEntity(final CacheKey key);

    boolean containsEntity(final Class<?> entityClass, final Long id);

    Map<CacheKey, Object> getDirtyEntities();

    void preLoad(final Class<?> entityClass, final Object primaryKey);

    void postLoad(final Object entity, final Long id);

    boolean contains(final CacheKey key);

    void prePersist(final Object entity, final Object primaryKey);

    void postPersist(final Object entity, final Object primaryKey);

    void handlePersistError(final CacheKey cacheKey);

    Status getEntityStatus(final CacheKey key);

    void markAsDeleted(final CacheKey key);
}
