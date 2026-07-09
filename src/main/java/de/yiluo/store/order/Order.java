package de.yiluo.store.order;

import de.yiluo.store.user.User;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private LocalDateTime orderDate;
    private BigDecimal totalAmount;
    private String status; // e.g., PENDING, SHIPPED, COMPLETED

    // in relational database, orderitem know order but not the reverse
    // mappedBy make it bidirectional, meaning order also know orderitem
    // mappedBy = "order" means orderitem is the owning side
    // cascade = CascadeType.ALL means save/delete/update order also save/delete/update orderitem record
    // but with only cascade = CascadeType.ALL, delete one item in order will only
    // make the orderitem point to null instead of an order
    // orphanRemoval = true means delete one item in order also delete one ordertiem record
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<OrderItem> getOrderItems() { return orderItems; }
    public void setOrderItems(List<OrderItem> orderItems) { this.orderItems = orderItems; }
}