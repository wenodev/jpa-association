package persistence.sql.dml;

import persistence.sql.ddl.TableName;

class SelectMaxIdQueryBuilder {
    private static final String SELECT_MAX_ID_TEMPLATE = "SELECT MAX(%s) FROM %s;";

    private final Class<?> entityClass;

    SelectMaxIdQueryBuilder(final Class<?> entityClass) {
        this.entityClass = entityClass;
    }

    String build() {
        final String tableName = new TableName(entityClass).value();
        final String idColumnName = new IdColumnName(entityClass).getIdColumnName();
        return SELECT_MAX_ID_TEMPLATE.formatted(idColumnName, tableName);
    }
}
