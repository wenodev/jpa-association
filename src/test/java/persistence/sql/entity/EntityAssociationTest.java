package persistence.sql.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import persistence.sql.ddl.Order;
import persistence.sql.ddl.OrderItem;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EntityAssociationTest {
    @Test
    @DisplayName("단일 엔티티의 연관관계 제거시 원본 엔티티를 반환한다")
    void withoutAssociation() {
        // arrange
        final OrderItem entity = new OrderItem(1L, "product-1", 10);
        final EntityAssociation entityAssociation = new EntityAssociation(entity);

        // act
        final Object result = entityAssociation.removeAssociations();

        // assert
        assertThat(result).isSameAs(entity);
    }

    @Test
    @DisplayName("연관된 엔티티들의 관계 제거시 빈 컬렉션을 가진 새 엔티티를 반환한다")
    void withAssociation() {
        // arrange
        final List<OrderItem> orderItems = List.of(new OrderItem(1L, "product-1", 10));
        final Order entity = new Order(1L, "order-1", orderItems);
        final EntityAssociation entityAssociation = new EntityAssociation(entity);

        // act
        final Order result = (Order) entityAssociation.removeAssociations();

        // assert
        assertThat(result.getOrderItems()).isNull();
    }
}
