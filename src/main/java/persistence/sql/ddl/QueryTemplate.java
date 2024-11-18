package persistence.sql.ddl;

enum QueryTemplate {
    DROP_TABLE("DROP TABLE IF EXISTS %s CASCADE;"),
    CREATE_TABLE("CREATE TABLE %s (\n%s);"),
    COLUMN_DEFINITION("    " + "%s %s%s%s%s");

    private final String template;

    QueryTemplate(final String template) {
        this.template = template;
    }

    public String format(final Object... args) {
        return String.format(template, args);
    }
}
