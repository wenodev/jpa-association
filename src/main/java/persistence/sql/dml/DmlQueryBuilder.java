package persistence.sql.dml;

public class DmlQueryBuilder {
    public String selectMaxId(final Class<?> clazz) {
        return new SelectMaxIdQueryBuilder(clazz).build();
    }

    public String select(final Class<?> clazz) {
        return new SelectQueryBuilder(clazz).build();
    }

    public String select(final Class<?> clazz, final Long id) {
        return new SelectQueryBuilder(clazz, id).build();
    }

    public String insert(final Class<?> clazz, final Object entity) {
        return new InsertQueryBuilder(clazz, entity).build();
    }

    public String update(final Class<?> clazz, final Object entity, final Long id) {
        return new UpdateQueryBuilder(clazz, entity, id).build();
    }

    public String delete(final Class<?> clazz, final Long id) {
        return new DeleteQueryBuilder(clazz, id).build();
    }
}
