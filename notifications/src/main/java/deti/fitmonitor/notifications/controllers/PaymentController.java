package deti.fitmonitor.notifications.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import deti.fitmonitor.notifications.models.Payments;
import deti.fitmonitor.notifications.services.StripeService;
import deti.fitmonitor.notifications.services.PaymentsService;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin("http://localhost:4200")
public class PaymentController {

    private PaymentsService paymentsService;

    private StripeService stripeService;

    @Autowired
    public PaymentController(PaymentsService paymentsService, StripeService stripeService) {
        this.paymentsService = paymentsService;
        this.stripeService = stripeService;
    }

    @PostMapping("/create-checkout-session")
    public Map<String, String> createCheckoutSession(@AuthenticationPrincipal String userSub, @RequestBody Map<String, Object> request) throws Exception {
        Long amount = Long.valueOf(request.get("amount").toString());
        String currency = request.get("currency").toString();
        String successUrl = request.get("successUrl").toString();
        String cancelUrl = request.get("cancelUrl").toString();
        return stripeService.createCheckoutSession(amount, currency, successUrl, cancelUrl, userSub);
    }

    @GetMapping("/user/subscriptiondate")
    public String getSubscriptionDate(@AuthenticationPrincipal String userSub) {
        Payments payment = paymentsService.getPayment(userSub);
        if (payment != null) {
            return payment.getSubscriptionDate().toString();
        }
        return null;
    }
    
}
