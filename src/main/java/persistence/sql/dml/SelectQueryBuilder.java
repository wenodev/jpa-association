package persistence.sql.dml;

import persistence.sql.ddl.TableName;

import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;

class SelectQueryBuilder {
    private static final String SELECT_ALL_TEMPLATE = "SELECT * FROM %s;";
    private static final String SELECT_BY_ID_TEMPLATE = "SELECT * FROM %s WHERE %s = %s;";
    private static final String SELECT_WITH_JOIN_TEMPLATE = """
            SELECT %s
            FROM %s p
            JOIN %s c ON p.id = c.%s
            WHERE p.id = %s;""";

    private final Class<?> entityClass;
    private final Long id;

    SelectQueryBuilder(final Class<?> entityClass) {
        this(entityClass, null);
    }

    SelectQueryBuilder(final Class<?> entityClass, final Long id) {
        this.entityClass = entityClass;
        this.id = id;
    }

    String build() {
        if (id == null) {
            return SELECT_ALL_TEMPLATE.formatted(new TableName(entityClass).value());
        }

        if (hasOneToManyAssociation(entityClass)) {
            return buildJoinQuery();
        }

        return buildSimpleSelectById();
    }

    private String buildSimpleSelectById() {
        final String tableName = new TableName(entityClass).value();
        final String idColumnName = new IdColumnName(entityClass).getIdColumnName();
        return SELECT_BY_ID_TEMPLATE.formatted(
                tableName,
                idColumnName,
                SqlValueFormatter.format(id)
        );
    }

    private String buildJoinQuery() {
        final Field joinField = findOneToManyField(entityClass);
        final Class<?> childType = getChildType(joinField);

        final String columns = new JoinColumnName(entityClass, childType).selectColumns();
        final String parentTable = new TableName(entityClass).value();
        final String childTable = new TableName(childType).value();
        final String joinColumnName = joinField.getAnnotation(JoinColumn.class).name();

        return SELECT_WITH_JOIN_TEMPLATE.formatted(
                columns,
                parentTable,
                childTable,
                joinColumnName,
                SqlValueFormatter.format(id)
        );
    }

    private boolean hasOneToManyAssociation(final Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .anyMatch(field -> field.isAnnotationPresent(OneToMany.class));
    }

    private Field findOneToManyField(final Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(OneToMany.class))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No @OneToMany association found"));
    }

    private Class<?> getChildType(final Field field) {
        return (Class<?>) ((ParameterizedType) field.getGenericType())
                .getActualTypeArguments()[0];
    }
}
