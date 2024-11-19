package persistence.sql.entity;

import jdbc.JdbcTemplate;
import persistence.sql.dml.DmlQueryBuilder;

import jakarta.persistence.OneToMany;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;

public class EntityPersister {
    private final JdbcTemplate jdbcTemplate;
    private final DmlQueryBuilder dmlQueryBuilder;

    public EntityPersister(final JdbcTemplate jdbcTemplate, final DmlQueryBuilder dmlQueryBuilder) {
        this.jdbcTemplate = jdbcTemplate;
        this.dmlQueryBuilder = dmlQueryBuilder;
    }

    void insert(final Object entity) {
        try {
            final Object entityToInsert = prepareEntityForInsert(entity);
            final String insert = dmlQueryBuilder.insert(entity.getClass(), entityToInsert);
            jdbcTemplate.execute(insert);
        } catch (final Exception e) {
            throw new RuntimeException("Failed to insert entity", e);
        }
    }

    private Object prepareEntityForInsert(final Object entity) {
        if (!hasOneToManyAssociation(entity.getClass())) {
            return entity;
        }
        return createEntityWithAssociations(entity);
    }

    private boolean hasOneToManyAssociation(final Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .anyMatch(field -> field.isAnnotationPresent(OneToMany.class));
    }

    private Object createEntityWithAssociations(final Object originalEntity) {
        final Object newEntity = createNewInstance(originalEntity.getClass());
        copyNonCollectionFields(originalEntity, newEntity);
        return newEntity;
    }

    private Object createNewInstance(final Class<?> entityClass) {
        try {
            final Constructor<?> constructor = entityClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (final Exception e) {
            throw new RuntimeException("Failed to create new instance", e);
        }
    }

    private void copyNonCollectionFields(final Object source, final Object target) {
        Arrays.stream(source.getClass().getDeclaredFields())
                .filter(field -> !field.isAnnotationPresent(OneToMany.class))
                .forEach(field -> copyField(field, source, target));
    }

    private void copyField(final Field field, final Object source, final Object target) {
        try {
            field.setAccessible(true);
            field.set(target, field.get(source));
        } catch (final IllegalAccessException e) {
            throw new RuntimeException("Failed to copy field: " + field.getName(), e);
        }
    }

    void update(final Object entity) {
        final EntityId entityId = new EntityId(entity);
        final String update = dmlQueryBuilder.update(entity.getClass(), entity, entityId.extractId());
        jdbcTemplate.execute(update);
    }

    void delete(final Object entity) {
        final EntityId entityId = new EntityId(entity);
        final String delete = dmlQueryBuilder.delete(entity.getClass(), entityId.extractId());
        jdbcTemplate.execute(delete);
    }
}
