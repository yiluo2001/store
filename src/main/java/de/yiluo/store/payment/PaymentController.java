package de.yiluo.store.payment;

import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;
import de.yiluo.store.order.OrderService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final OrderService orderService;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    public PaymentController(PaymentService paymentService, OrderService orderService) {
        this.paymentService = paymentService;
        this.orderService = orderService;
    }

    // service and controller have same method name
    // bc they do the same thing, just waiter vs cook
    @PostMapping("/create-intent")
    public ResponseEntity<PaymentResponse> createPaymentIntent(@RequestBody PaymentRequest request) {
        // webhookSecret is password shared by me and stripe
        // jsonPayload is info about the transaction
        // sigHeader should be hashed result of jsonPayload and webhookSecret
        // here I hash jsonPayload and webhookSecret myself to compare with sigHeader
        // matched meaning sigHeader is indeed sent by stripe
        try {
            PaymentResponse response = paymentService.createPaymentIntent(request.getOrderId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("Error initializing payment intent: " + e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    // webhook is to handle post request from stripe
    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String jsonPayload,
            // "Stripe-Signature" is a key in http header
            @RequestHeader("Stripe-Signature") String sigHeader) {

        Event event;

        try {
            event = Webhook.constructEvent(jsonPayload, sigHeader, webhookSecret);
        } catch (Exception e) {
            System.out.println("Webhook signature verification failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        }

        if ("payment_intent.succeeded".equals(event.getType())) {
            EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
            StripeObject stripeObject;

            // safe fallback if Stripe API Dashboard version doesn't perfectly match local pom.xml library versions
            if (dataObjectDeserializer.getObject().isPresent()) {
                // .get() to get content from optional wrapper
                stripeObject = dataObjectDeserializer.getObject().get();
            } else {
                System.out.println("Warning: API version layout mismatch detected. Using unsafe deserializer fallback pattern mapping.");
                try {
                    stripeObject = dataObjectDeserializer.deserializeUnsafe();
                } catch (Exception e) {
                    System.out.println("Fatal: Deserialization strategy completely failed: " + e.getMessage());
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Object compilation error");
                }
            }

            try {
                PaymentIntent paymentIntent = (PaymentIntent) stripeObject;
                String orderIdStr = paymentIntent.getMetadata().get("order_id");

                if (orderIdStr != null) {
                    Long orderId = Long.parseLong(orderIdStr);
                    orderService.updateOrderStatus(orderId, "PAID");
                    System.out.println("Order " + orderId + " successfully marked as PAID.");
                } else {
                    System.out.println("Warning: Received payment_intent.succeeded but 'order_id' was missing from metadata.");
                    return ResponseEntity.badRequest().body("Missing order_id metadata");
                }
            } catch (Exception e) {
                System.out.println("Error processing database update details:");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Database error");
            }
            
        } else {
            System.out.println("Received unhandled event type stream payload: " + event.getType());
        }

        // return a 200 OK to Stripe
        return ResponseEntity.ok("Success");
    }
}