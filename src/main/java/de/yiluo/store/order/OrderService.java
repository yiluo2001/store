package de.yiluo.store.order;

import de.yiluo.store.cart.CartItem;
import de.yiluo.store.cart.CartRepository;
import de.yiluo.store.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;

    public OrderService(OrderRepository orderRepository, CartRepository cartRepository) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
    }

    // convert CartItems into Order
    public Order checkout(User user) {
        List<CartItem> cartItems = cartRepository.findByUser(user);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cannot checkout an empty shopping cart.");
        }

        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PENDING");

        BigDecimal total = BigDecimal.ZERO;

        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());

            // note that this would cause n+1 problem
            // if CartRepository findByUser is not done with join fetch
            BigDecimal itemPrice = cartItem.getProduct().getPrice();
            orderItem.setPriceAtPurchase(itemPrice);

            BigDecimal itemTotal = itemPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            total = total.add(itemTotal);

            order.getOrderItems().add(orderItem);
        }

        order.setTotalAmount(total);

        Order savedOrder = orderRepository.save(order);

        cartRepository.deleteByUser(user);

        return savedOrder;
    }

    public List<Order> getUserOrderHistory(User user) {
        return orderRepository.findByUserOrderByOrderDateDesc(user);
    }

    public Order updateOrderStatus(Long orderId, String newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        order.setStatus(newStatus);
        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
}