package persistence.sql.dml;


import jakarta.persistence.Column;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.stream.Stream;

class JoinColumnName {
    private static final String PARENT_ALIAS = "p";
    private static final String CHILD_ALIAS = "c";

    private final Class<?> parentClass;
    private final Class<?> childClass;

    JoinColumnName(final Class<?> parentClass, final Class<?> childClass) {
        this.parentClass = parentClass;
        this.childClass = childClass;
    }

    String selectColumns() {
        final Stream<String> parentColumns = extractColumns(parentClass, PARENT_ALIAS);
        final Stream<String> childColumns = extractColumns(childClass, CHILD_ALIAS);

        return Stream.concat(parentColumns, childColumns)
                .reduce((a, b) -> a + ", " + b)
                .orElseThrow(() -> new IllegalStateException("No columns found"));
    }

    private Stream<String> extractColumns(final Class<?> clazz, final String alias) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(this::isIncludedField)
                .map(field -> formatColumn(field, alias));
    }

    private boolean isIncludedField(final Field field) {
        return !field.isAnnotationPresent(Transient.class)
               && !field.isAnnotationPresent(OneToMany.class);
    }

    private String formatColumn(final Field field, final String alias) {
        final String columnName = extractColumnName(field);
        return String.format("%s.%s", alias, columnName);
    }

    private String extractColumnName(final Field field) {
        final Column column = field.getAnnotation(Column.class);
        if (column != null && !column.name().isEmpty()) {
            return column.name();
        }
        return camelToSnakeCase(field.getName());
    }

    private String camelToSnakeCase(final String str) {
        return str.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }
}
