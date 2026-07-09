package de.yiluo.store.cart;

import de.yiluo.store.product.Product;
import de.yiluo.store.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<CartItem, Long> {
    // JOIN FETCH: inner join the CartItem and Product, hydrate Product
    // to avoid n+1 problem
    @Query("SELECT c FROM CartItem c JOIN FETCH c.product WHERE c.user = :user")
    List<CartItem> findByUser(@Param("user") User user);
    Optional<CartItem> findByUserAndProduct(User user, Product product);
    void deleteByUser(User user);
}