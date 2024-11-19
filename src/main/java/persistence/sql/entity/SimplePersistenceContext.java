package persistence.sql.entity;

import jakarta.persistence.Entity;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

class SimplePersistenceContext implements PersistenceContext {
    private final Map<CacheKey, EntityState> entityStates = new HashMap<>();

    @Override
    public Optional<Object> getEntity(final CacheKey key) {
        return Optional.ofNullable(entityStates.get(key))
                .map(EntityState::getEntity);
    }

    @Override
    public void managedEntity(final CacheKey key, final Object entity) {
        entityStates.put(key, new EntityState(entity, Status.MANAGED));
    }

    @Override
    public void removeEntity(final CacheKey key) {
        final EntityState state = entityStates.get(key);
        if (state != null) {
            state.makeDeleted();
            entityStates.remove(key);
        }
    }

    @Override
    public boolean containsEntity(final Class<?> entityClass, final Long id) {
        final CacheKey key = createCacheKey(entityClass, id);
        return entityStates.containsKey(key);
    }

    @Override
    public Map<CacheKey, Object> getDirtyEntities() {
        return entityStates.entrySet().stream()
                .filter(entry -> entry.getValue().isDirty())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().getEntity()
                ));
    }

    @Override
    public void preLoad(final Class<?> entityClass, final Object primaryKey) {
        final CacheKey cacheKey = createCacheKey(entityClass, (Long) primaryKey);
        final EntityState state = new EntityState(entityClass, Status.LOADING);
        entityStates.put(cacheKey, state);
    }

    @Override
    public void postLoad(final Object entity, final Long id) {
        final CacheKey cacheKey = createCacheKey(entity.getClass(), id);
        entityStates.put(cacheKey, new EntityState(entity, Status.MANAGED));
    }

    @Override
    public boolean contains(final CacheKey key) {
        return entityStates.containsKey(key);
    }

    @Override
    public void prePersist(final Object entity, final Object primaryKey) {
        final CacheKey key = createCacheKey(entity.getClass(), primaryKey);
        entityStates.put(key, new EntityState(entity, Status.SAVING));
    }

    @Override
    public void postPersist(final Object entity, final Object primaryKey) {
        final CacheKey key = createCacheKey(entity.getClass(), primaryKey);
        entityStates.put(key, new EntityState(entity, Status.MANAGED));
    }

    @Override
    public void handlePersistError(final CacheKey cacheKey) {
        entityStates.remove(cacheKey);
    }

    @Override
    public Status getEntityStatus(final CacheKey key) {
        return Optional.ofNullable(entityStates.get(key))
                .map(EntityState::getStatus)
                .orElse(null);
    }

    @Override
    public void markAsDeleted(final CacheKey key) {
        Optional.ofNullable(entityStates.get(key))
                .ifPresent(EntityState::makeDeleted);
    }

    private CacheKey createCacheKey(final Class<?> entityClass, final Object id) {
        validateEntity(entityClass);
        return new CacheKey(entityClass.getSimpleName(), id);
    }

    private void validateEntity(final Class<?> entityClass) {
        if (!entityClass.isAnnotationPresent(Entity.class)) {
            throw new IllegalArgumentException("Class must be annotated with @Entity");
        }
    }
}