package deti.fitmonitor.payments.services;

import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

@Service
public class StripeService {

    private static final Logger logger = LoggerFactory.getLogger(StripeService.class);

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    public Map<String, String> createCheckoutSession(Long amount, String currency, String successUrl, String cancelUrl, String userSub) throws Exception {

        Stripe.apiKey = stripeApiKey;

        try {
            SessionCreateParams params = SessionCreateParams.builder()
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(
                                SessionCreateParams.LineItem.PriceData.builder()
                                        .setCurrency(currency)
                                        .setUnitAmount(amount)
                                        .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                .setName("Your Product Name")
                                                .build())
                                        .build())
                        .build())
                        .putMetadata("userSub", userSub)
                .build();
            Session session = Session.create(params);

            logger.info("Stripe checkout session created successfully. Session ID: {}", session.getId());

            // Return the session ID in the response
            Map<String, String> response = new HashMap<>();
            response.put("url", session.getUrl());
            return response;

        } catch (Exception e) {
            logger.error("Error occurred while craftereating the Stripe checkout session", e);
            throw new Exception("Error occurred while creating the Stripe checkout session");
        }
    }
}
