package de.yiluo.store.payment;

import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import de.yiluo.store.order.Order;
import de.yiluo.store.order.OrderRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    private final OrderRepository orderRepository;

    // @Value("${stripe.api.key}") load secret key from application.properties
    public PaymentService(OrderRepository orderRepository, @Value("${stripe.api.key}") String apiKey) {
        this.orderRepository = orderRepository;
        Stripe.apiKey = apiKey;
    }

    public PaymentResponse createPaymentIntent(Long orderId) throws Exception {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Stripe process currencies in their smallest unit (e.g., Cents for USD/EUR).
        // $10.50 must be sent as 1050 cents.
        // .longValue() bc stripe only allow integer
        long amountInCents = order.getTotalAmount().multiply(new java.math.BigDecimal(100)).longValue();

        // info stripe needs to create intent
        // Stripe.apiKey = apiKey; is being used under the table
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency("eur")
                .putMetadata("order_id", orderId.toString())
                .build();

        // intent is then created by stripe
        PaymentIntent intent = PaymentIntent.create(params);

        return new PaymentResponse(intent.getClientSecret(), intent.getId());
    }
}