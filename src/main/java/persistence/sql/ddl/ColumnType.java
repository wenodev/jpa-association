package persistence.sql.ddl;

import java.util.Arrays;

enum ColumnType {
    BIGINT(Long.class),
    INTEGER(Integer.class),
    VARCHAR(String.class);

    private final Class<?> javaType;

    ColumnType(final Class<?> javaType) {
        this.javaType = javaType;
    }

    static ColumnType fromJavaType(final Class<?> type) {
        return Arrays.stream(values())
                .filter(columnType -> columnType.javaType.equals(type))
                .findFirst()
                .orElse(VARCHAR);
    }

    String getDefinition(final DatabaseDialect dialect) {
        return dialect.getColumnTypeDefinition(this);
    }
}
