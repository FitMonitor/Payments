package deti.fitmonitor.notifications.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import deti.fitmonitor.notifications.services.StripeService;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin("*")
public class PaymentController {

    @Autowired
    private StripeService stripeService;

    @PostMapping("/create-checkout-session")
    public Map<String, String> createCheckoutSession(@RequestBody Map<String, Object> request) throws Exception {
        Long amount = Long.valueOf(request.get("amount").toString());
        String currency = request.get("currency").toString();
        String successUrl = request.get("successUrl").toString();
        String cancelUrl = request.get("cancelUrl").toString();
        return stripeService.createCheckoutSession(amount, currency, successUrl, cancelUrl);
    }
    
}
