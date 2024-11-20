package persistence.sql.dml;

import persistence.sql.ddl.TableName;

import java.util.Map;
import java.util.stream.Collectors;

class UpdateQueryBuilder {
    private static final String UPDATE_TEMPLATE = "UPDATE %s SET %s WHERE %s = %s;";

    private final Class<?> entityClass;
    private final Object entity;
    private final Long id;

    UpdateQueryBuilder(final Class<?> entityClass, final Object entity, final Long id) {
        this.entityClass = entityClass;
        this.entity = entity;
        this.id = id;
    }

    String build() {
        final String tableName = new TableName(entityClass).value();
        final String setClause = formatSetClause();
        final String idColumnName = new IdColumnName(entityClass).getIdColumnName();

        return UPDATE_TEMPLATE.formatted(
                tableName,
                setClause,
                idColumnName,
                SqlValueFormatter.format(id)
        );
    }

    private String formatSetClause() {
        final Map<String, Object> updateValues = new UpdateValues().value(entity);

        return updateValues.entrySet().stream()
                .map(entry -> String.format("%s = %s",
                        entry.getKey(),
                        SqlValueFormatter.format(entry.getValue())))
                .collect(Collectors.joining(", "));
    }
}
