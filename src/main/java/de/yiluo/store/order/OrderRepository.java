package de.yiluo.store.order;

import de.yiluo.store.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    // DISTINCT bc otherwise parent is duplicated due to multiple children
    // in this case, multiple orderItem with one same Order
    @Query("SELECT DISTINCT o FROM Order o " +
            "JOIN FETCH o.orderItems i " +
            "JOIN FETCH i.product " +
            "WHERE o.user = :user " +
            "ORDER BY o.orderDate DESC")
    List<Order> findByUserOrderByOrderDateDesc(User user); // history sorted by newest first
}