package deti.fitmonitor.notifications.controllers;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(StripeWebhookController.class);

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
                    logger.info("Payment successful: " + event.toString());
                    stripeWebhookService.handlePaymentSucceededEvent(event.toString());
                    break;
                default:
                    logger.info("Unhandled event type: " + event.getType());
            }
        } catch (Exception e) {
            logger.error("Error while handling webhook: " + e.getMessage());
        }
    }
    
}