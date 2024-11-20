package persistence.sql.entity;

import jdbc.JdbcTemplate;
import persistence.sql.dml.DmlQueryBuilder;

public class EntityPersister {
    private final JdbcTemplate jdbcTemplate;
    private final DmlQueryBuilder dmlQueryBuilder;

    public EntityPersister(final JdbcTemplate jdbcTemplate, final DmlQueryBuilder dmlQueryBuilder) {
        this.jdbcTemplate = jdbcTemplate;
        this.dmlQueryBuilder = dmlQueryBuilder;
    }

    void insert(final Object entity) {
        try {
            final EntityAssociation entityAssociation = new EntityAssociation(entity);
            final Object entityToInsert = entityAssociation.removeAssociations();
            final String insert = dmlQueryBuilder.insert(entityToInsert.getClass(), entityToInsert);
            jdbcTemplate.execute(insert);
        } catch (final Exception e) {
            throw new RuntimeException("Failed to insert entity", e);
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
