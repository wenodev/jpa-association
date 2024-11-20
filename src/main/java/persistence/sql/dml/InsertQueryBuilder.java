package persistence.sql.dml;

import persistence.sql.ddl.TableName;

import java.util.List;
import java.util.stream.Collectors;

class InsertQueryBuilder {
    private static final String INSERT_TEMPLATE = "INSERT INTO %s (%s) VALUES (%s);";

    private final Class<?> entityClass;
    private final Object entity;

    InsertQueryBuilder(final Class<?> entityClass, final Object entity) {
        this.entityClass = entityClass;
        this.entity = entity;
    }

    String build() {
        final String tableName = new TableName(entityClass).value();
        final String columns = formatColumns(entityClass);
        final List<Object> values = new InsertValues().value(entity);

        return INSERT_TEMPLATE.formatted(
                tableName,
                columns,
                formatSqlValues(values)
        );
    }

    private String formatColumns(final Class<?> clazz) {
        return String.join(", ", new ColumnName(clazz).value());
    }

    private String formatSqlValues(final List<Object> values) {
        return values.stream()
                .map(SqlValueFormatter::format)
                .collect(Collectors.joining(", "));
    }
}
