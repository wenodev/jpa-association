package persistence.sql.dml;

import persistence.sql.ddl.TableName;

class DeleteQueryBuilder {
    private static final String DELETE_TEMPLATE = "DELETE FROM %s WHERE %s = %s;";

    private final Class<?> entityClass;
    private final Long id;

    DeleteQueryBuilder(final Class<?> entityClass, final Long id) {
        this.entityClass = entityClass;
        this.id = id;
    }

    String build() {
        final String tableName = new TableName(entityClass).value();
        final String idColumnName = new IdColumnName(entityClass).getIdColumnName();

        return DELETE_TEMPLATE.formatted(
                tableName,
                idColumnName,
                SqlValueFormatter.format(id)
        );
    }
}
