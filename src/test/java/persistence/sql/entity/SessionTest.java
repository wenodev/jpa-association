package persistence.sql.entity;

import database.DatabaseServer;
import database.H2;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import persistence.TestJdbcTemplate;
import persistence.sql.ddl.DdlQueryBuilder;
import persistence.sql.ddl.H2Dialect;
import persistence.sql.ddl.Order;
import persistence.sql.ddl.OrderItem;
import persistence.sql.ddl.Person;
import persistence.sql.dml.DmlQueryBuilder;

import java.sql.Connection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SessionTest {
    private DatabaseServer server;
    private TestJdbcTemplate jdbcTemplate;
    private DdlQueryBuilder ddlQueryBuilder;
    private DmlQueryBuilder dmlQueryBuilder;
    private Session entityManager;

    @BeforeEach
    void setUp() throws Exception {
        server = new H2();
        server.start();
        final Connection connection = server.getConnection();
        jdbcTemplate = new TestJdbcTemplate(connection);
        ddlQueryBuilder = new DdlQueryBuilder(new H2Dialect());
        dmlQueryBuilder = new DmlQueryBuilder();
        deleteIfTableExists(Person.class);
        createTableAndVerify(Person.class);
        deleteIfTableExists(OrderItem.class);
        createTableAndVerify(OrderItem.class);
        deleteIfTableExists(Order.class);
        createTableAndVerify(Order.class);
        final EntityPersister entityPersister = new EntityPersister(jdbcTemplate, dmlQueryBuilder);
        final EntityLoader entityLoader = new EntityLoader(jdbcTemplate, dmlQueryBuilder);
        final PersistenceContext persistenceContext = new PersistenceContext();
        entityManager = new Session(entityPersister, entityLoader, persistenceContext);
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    @DisplayName("연관관계가 있는 엔티티를 조회할 수 있다.")
    @Test
    void findWithAssociation() {
        // given
        final OrderItem item1 = new OrderItem(1L, "Product 1", 2);
        final OrderItem item2 = new OrderItem(2L, "Product 2", 3);
        entityManager.persist(item1);
        entityManager.persist(item2);

        final Order order = new Order(1L, "ORDER-1", List.of(item1, item2));
        entityManager.persist(order);
        entityManager.flush();

        // when
        final Order foundOrder = entityManager.find(Order.class, order.getId());

        // then
        assertThat(foundOrder.getOrderNumber()).isEqualTo("ORDER-1");
        assertThat(foundOrder.getOrderItems())
                .hasSize(2)
                .flatExtracting(OrderItem::getProduct, OrderItem::getQuantity)
                .containsExactlyInAnyOrder("Product 1", 2, "Product 2", 3);
    }

    @DisplayName("더티 체크가 잘 동작하는지 검증한다.")
    @Test
    void dirtyCheckTest() {
        final Person expectedPerson = new Person(1L, "Kent Beck", 64, "beck@example.com");
        entityManager.persist(expectedPerson);

        expectedPerson.updateEmail("kentbeck@example.com");
        entityManager.flush();

        final Person actualPerson = entityManager.find(Person.class, 1L);
        assertThat(actualPerson.getEmail()).isEqualTo("kentbeck@example.com");
    }

    @DisplayName("1차 캐시가 잘 동작하는지 검증한다.")
    @Test
    void firstLevelCacheTest() {
        final Person expectedPerson = new Person(1L, "Kent Beck", 64, "beck@example.com");
        entityManager.persist(expectedPerson);

        // 처음 조회 시에는 1차 캐시에 없으므로 DB에서 조회
        final Person actualPerson = entityManager.find(Person.class, 1L);
        assertThat(actualPerson.getId()).isEqualTo(expectedPerson.getId());

        // 두 번째 조회 시에는 1차 캐시에서 조회
        final Person cachedPerson = entityManager.find(Person.class, 1L);
        assertThat(cachedPerson).isSameAs(actualPerson);
    }

    @DisplayName("Person 객체를 저장하고 조회하고 수정하고 삭제한다.")
    @Test
    void scenario() {
        final Person expectedPerson = new Person(1L, "Kent Beck", 64, "beck@example.com");
        entityManager.persist(expectedPerson);

        final Person actualPerson = entityManager.find(Person.class, 1L);
        assertThat(actualPerson.getId()).isEqualTo(expectedPerson.getId());

        final Person personToUpdate = new Person(1L, "Kent Beck", 60, "youngBeck@example.com");
        entityManager.merge(personToUpdate);

        final Person updatedPerson = entityManager.find(Person.class, 1L);
        assertThat(updatedPerson.getAge()).isEqualTo(60);

        entityManager.remove(updatedPerson);
        assertRemove();
    }

    private void assertRemove() {
        final List<Person> query = jdbcTemplate.query(dmlQueryBuilder.select(Person.class), resultSet -> {
            resultSet.next();
            return new Person(
                    resultSet.getLong("id"),
                    resultSet.getString("nick_name"),
                    resultSet.getInt("old"),
                    resultSet.getString("email")
            );
        });
        assertThat(query).hasSize(0);
    }

    private void createTableAndVerify(final Class<?> clazz) {
        createTable(clazz);
        assertTableCreated(clazz);
    }

    private void assertTableCreated(final Class<?> clazz) {
        assertTrue(jdbcTemplate.doesTableExist(clazz), "Table was not created.");
    }

    private void createTable(final Class<?> clazz) {
        final String createSql = ddlQueryBuilder.create(clazz);
        jdbcTemplate.execute(createSql);
    }

    private void deleteIfTableExists(final Class<?> clazz) {
        if (jdbcTemplate.doesTableExist(clazz)) {
            final String dropSql = ddlQueryBuilder.drop(clazz);
            jdbcTemplate.execute(dropSql);
        }
    }
}
