package deti.fitmonitor.notifications.controllers;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import deti.fitmonitor.notifications.services.StripeWebhookService;

@RestController
@RequestMapping("/api/webhooks")
public class StripeWebhookController {
    @Value("${stripe.webhook.secret}")
    private String endpointSecret;

    private final StripeWebhookService stripeWebhookService;

    public StripeWebhookController(StripeWebhookService stripeWebhookService) {
        this.stripeWebhookService = stripeWebhookService;
    }

    @PostMapping
    public void handleWebhook(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) {
        try {
            Event event = Webhook.constructEvent(payload, sigHeader, endpointSecret);

            switch (event.getType()) {
                case "checkout.session.completed":
                    // Handle successful payment
                    System.out.println("Payment successful: " + event.toString());
                    stripeWebhookService.handlePaymentSucceededEvent(event.toString());
                    break;
                default:
                    System.out.println("Unhandled event type: " + event.getType());
            }
        } catch (Exception e) {
            System.out.println("Error while handling webhook: " + e.getMessage());
        }
    }
    
    @GetMapping("/x")
    public String getHello(){
        return "Hello World";
    }
}