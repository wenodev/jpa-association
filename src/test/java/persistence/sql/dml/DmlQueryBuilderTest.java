package persistence.sql.dml;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import persistence.sql.ddl.Order;
import persistence.sql.ddl.Person;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DmlQueryBuilderTest {
    @DisplayName("연관관계가 있는 엔티티를 조회하는 SELECT 쿼리를 생성한다.")
    @Test
    void selectWithAssociation() {
        final DmlQueryBuilder dmlQueryBuilder = new DmlQueryBuilder();
        final String actualQuery = dmlQueryBuilder.select(Order.class, 1L);
        assertEquals(expectedSelectWithAssociation(), actualQuery);
    }

    @DisplayName("클래스 정보를 바탕으로 INSERT 쿼리를 생성한다.")
    @Test
    void insert() {
        final DmlQueryBuilder dmlQueryBuilder = new DmlQueryBuilder();
        final Person person = new Person(1L, "Kent Beck", 64, "beck@example.com");
        assertEquals(expectedForInsert(), dmlQueryBuilder.insert(Person.class, person));
    }

    @DisplayName("클래스 정보를 바탕으로 리스트를 위한 SELECT 쿼리를 생성한다.")
    @Test
    void findAll() {
        final DmlQueryBuilder dmlQueryBuilder = new DmlQueryBuilder();
        assertEquals(expectedForFindAll(), dmlQueryBuilder.select(Person.class));
    }

    @DisplayName("클래스 정보를 바탕으로 단건을 위한 SELECT 쿼리를 생성한다.")
    @Test
    void findById() {
        final DmlQueryBuilder dmlQueryBuilder = new DmlQueryBuilder();
        assertEquals(expectedForFindById(), dmlQueryBuilder.select(Person.class, 1L));
    }

    @DisplayName("클래스 정보를 바탕으로 MAX ID를 조회하는 SELECT 쿼리를 생성한다.")
    @Test
    void selectMaxId() {
        final DmlQueryBuilder dmlQueryBuilder = new DmlQueryBuilder();
        assertEquals("SELECT MAX(id) FROM USERS;", dmlQueryBuilder.selectMaxId(Person.class));
    }

    @DisplayName("클래스 정보를 바탕으로 DELETE 쿼리를 생성한다.")
    @Test
    void delete() {
        final DmlQueryBuilder dmlQueryBuilder = new DmlQueryBuilder();
        assertEquals(expectedForDelete(), dmlQueryBuilder.delete(Person.class, 1L));
    }

    private String expectedSelectWithAssociation() {
        return """
                SELECT p.id, p.order_number, c.id, c.product, c.quantity
                FROM ORDERS p
                JOIN ORDER_ITEMS c ON p.id = c.order_id
                WHERE p.id = 1;""";
    }

    private String expectedForDelete() {
        return "DELETE FROM USERS WHERE id = 1;";
    }

    private String expectedForFindById() {
        return "SELECT * FROM USERS WHERE id = 1;";
    }

    private String expectedForFindAll() {
        return "SELECT * FROM USERS;";
    }

    private String expectedForInsert() {
        return "INSERT INTO USERS (id, nick_name, old, email) VALUES (1, 'Kent Beck', 64, 'beck@example.com');";
    }
}
