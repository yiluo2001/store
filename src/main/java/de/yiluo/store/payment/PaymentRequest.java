package de.yiluo.store.payment;

// dto
public class PaymentRequest {
    private Long orderId;

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    // so frontend send order id to backend
    // backend generate client secret and payment intent using api key
    // front end use public key and client secret and payment intent to make payment through stripe
}