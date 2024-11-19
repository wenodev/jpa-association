package persistence.sql.dml;

import persistence.sql.ddl.TableName;

import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DmlQueryBuilder {
    private static final String SELECT_MAX_ID_TEMPLATE = "SELECT MAX(%s) FROM %s;";
    private static final String SELECT_ALL_TEMPLATE = "SELECT * FROM %s;";
    private static final String SELECT_BY_ID_TEMPLATE = "SELECT * FROM %s WHERE %s = %s;";
    private static final String INSERT_TEMPLATE = "INSERT INTO %s (%s) VALUES (%s);";
    private static final String DELETE_TEMPLATE = "DELETE FROM %s WHERE %s = %s;";
    private static final String UPDATE_TEMPLATE = "UPDATE %s SET %s WHERE %s = %s;";
    private static final String SELECT_WITH_JOIN_TEMPLATE = """
            SELECT %s
            FROM %s p
            JOIN %s c ON p.id = c.%s
            WHERE p.id = %s;""";


    public String update(final Class<?> clazz, final Object entity, final Long id) {
        final String tableName = new TableName(clazz).value();
        final String setClause = formatSetClause(entity);
        final String idColumnName = new IdColumnName(clazz).getIdColumnName();

        return UPDATE_TEMPLATE.formatted(
                tableName,
                setClause,
                idColumnName,
                formatSqlValue(id)
        );
    }

    public String delete(final Class<?> clazz, final Long id) {
        final String tableName = new TableName(clazz).value();
        final String idColumnName = new IdColumnName(clazz).getIdColumnName();
        return DELETE_TEMPLATE.formatted(
                tableName,
                idColumnName,
                formatSqlValue(id)
        );
    }

    public String selectMaxId(final Class<?> clazz) {
        final String tableName = new TableName(clazz).value();
        final String idColumnName = new IdColumnName(clazz).getIdColumnName();
        return SELECT_MAX_ID_TEMPLATE.formatted(idColumnName, tableName);
    }

    public String select(final Class<?> clazz) {
        final String tableName = new TableName(clazz).value();
        return SELECT_ALL_TEMPLATE.formatted(tableName);
    }

    public String select(final Class<?> clazz, final Long id) {
        if (hasOneToManyAssociation(clazz)) {
            return buildJoinQuery(clazz, id);
        }

        final String tableName = new TableName(clazz).value();
        final String idColumnName = new IdColumnName(clazz).getIdColumnName();
        return SELECT_BY_ID_TEMPLATE.formatted(
                tableName,
                idColumnName,
                formatSqlValue(id)
        );
    }

    public String insert(final Class<?> clazz, final Object object) {
        final String tableName = new TableName(clazz).value();
        final String columns = formatColumns(clazz);
        final List<Object> value = new InsertValues().value(object);

        return String.format(INSERT_TEMPLATE, tableName, columns, formatSqlValues(value));
    }

    private String formatSetClause(final Object entity) {
        final Map<String, Object> updateValues = new UpdateValues().value(entity);

        return updateValues.entrySet().stream()
                .map(entry -> String.format("%s = %s",
                        entry.getKey(),
                        formatSqlValue(entry.getValue())))
                .collect(Collectors.joining(", "));
    }

    private boolean hasOneToManyAssociation(final Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .anyMatch(field -> field.isAnnotationPresent(OneToMany.class));
    }

    private String buildJoinQuery(final Class<?> clazz, final Long id) {
        final Field joinField = findOneToManyField(clazz);
        final Class<?> childType = getChildType(joinField);

        final String columns = new JoinColumnName(clazz, childType).selectColumns();
        final String parentTable = new TableName(clazz).value();
        final String childTable = new TableName(childType).value();
        final String joinColumnName = joinField.getAnnotation(JoinColumn.class).name();

        return SELECT_WITH_JOIN_TEMPLATE.formatted(
                columns,
                parentTable,
                childTable,
                joinColumnName,
                formatSqlValue(id)
        );
    }

    private Class<?> getChildType(final Field field) {
        return (Class<?>) ((ParameterizedType) field.getGenericType())
                .getActualTypeArguments()[0];
    }

    private Field findOneToManyField(final Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(OneToMany.class))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No @OneToMany association found"));
    }

    private String formatColumns(final Class<?> clazz) {
        final List<String> columns = new ColumnName(clazz).value();
        return String.join(", ", columns);
    }

    private String formatSqlValues(final List<Object> value) {
        return value.stream()
                .map(this::formatSqlValue)
                .reduce((a, b) -> a + ", " + b)
                .orElseThrow();
    }

    private String formatSqlValue(final Object value) {
        return switch (value) {
            case null -> "NULL";
            case final String s -> String.format("'%s'", s.replace("'", "''"));
            case final Number number -> value.toString();
            default -> String.format("'%s'", value);
        };
    }
}
