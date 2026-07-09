package de.yiluo.store.payment;

// dto
public class PaymentRequest {
    private Long orderId;

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
}