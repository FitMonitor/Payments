package deti.fitmonitor.notifications.controllers;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import deti.fitmonitor.notifications.services.StripeWebhookService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/default/api/payments/webhooks")
public class StripeWebhookController {
    @Value("${stripe.webhook.secret}")
    private String endpointSecret;

    private final StripeWebhookService stripeWebhookService;

    private static final Logger logger = LoggerFactory.getLogger(StripeWebhookController.class);

    public StripeWebhookController(StripeWebhookService stripeWebhookService) {
        this.stripeWebhookService = stripeWebhookService;
    }

    @PostMapping
    @Operation(summary = "Handle Stripe webhook events")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Webhook event handled"),
        @ApiResponse(responseCode = "400", description = "Error handling webhook event")
    })
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