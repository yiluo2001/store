package de.yiluo.store.order;

import de.yiluo.store.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserOrderByOrderDateDesc(User user); // history sorted by newest first
}