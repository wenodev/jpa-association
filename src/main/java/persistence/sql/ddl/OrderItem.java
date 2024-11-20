package persistence.sql.ddl;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Table(name = "order_items")
@Entity
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String product;

    private Integer quantity;

    public OrderItem(final Long id, final String product, final Integer quantity) {
        this.id = id;
        this.product = product;
        this.quantity = quantity;
    }

    protected OrderItem() {
    }

    public String getProduct() {
        return product;
    }

    public Integer getQuantity() {
        return quantity;
    }

    @Override
    public String toString() {
        return "OrderItem{" +
               "id=" + id +
               ", product='" + product + '\'' +
               ", quantity=" + quantity +
               '}';
    }
}
