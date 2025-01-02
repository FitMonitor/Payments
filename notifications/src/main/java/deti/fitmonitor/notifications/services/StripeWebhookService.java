package deti.fitmonitor.notifications.services;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import deti.fitmonitor.notifications.models.Payments;
import deti.fitmonitor.notifications.services.paymentsService;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import org.slf4j.Logger;

@Service
public class StripeWebhookService {

    private static final Logger logger = LoggerFactory.getLogger(StripeWebhookService.class);


    private final paymentsService paymentsService;

    public StripeWebhookService(paymentsService paymentsService) {
        this.paymentsService = paymentsService;
    }

    public void handlePaymentSucceededEvent(String event) {
        try {
            String jsonString = event.substring(event.indexOf('{'));
            // Parse the event JSON
            JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();

            // Extract userSub from the metadata
            JsonElement dataElement = jsonObject.get("data");
            if (dataElement == null || !dataElement.isJsonObject()) {
                logger.error("Expected 'data' to be a JSON object, but it is not.");
                return;
            }
            JsonObject dataObject = dataElement.getAsJsonObject();
            
            JsonElement objectElement = dataObject.get("object");
            if (objectElement == null || !objectElement.isJsonObject()) {
                logger.error("Expected 'object' to be a JSON object, but it is not.");
                return;
            }
            JsonObject sessionObject = objectElement.getAsJsonObject();

            // Extract userSub from the metadata
            JsonElement metadataElement = sessionObject.get("metadata");
            if (metadataElement == null || !metadataElement.isJsonObject()) {
                logger.error("Expected 'metadata' to be a JSON object, but it is not.");
                return;
            }
            JsonObject metadataObject = metadataElement.getAsJsonObject();
            
            String userSub = metadataObject.get("userSub") != null ? metadataObject.get("userSub").getAsString() : null;
            if (userSub == null || userSub.isEmpty()) {
                logger.error("No userSub found in the Stripe event metadata.");
                return;
            }

            String amount = sessionObject.get("amount_total").getAsString();
            Integer days = getSubscriptionDaysByAmount(amount);

            logger.info("Processing payment for userSub: {}", userSub);

            // Get or create a payment record
            Payments payment = paymentsService.getPayment(userSub);
            if (payment == null) {
                payment = new Payments();
                payment.setUserSub(userSub);
            }
            //save the payment
            paymentsService.savePayment(payment);

            // Increase the subscription by the number of days
            Date subscriptionDate = increaseSubscription(userSub, days);
            if (subscriptionDate != null) {
                logger.info("Subscription for userSub {} increased by {} days. New subscription date: {}", userSub, days, subscriptionDate);
            } else {
                logger.error("Error occurred while increasing the subscription for userSub: {}", userSub);
            }

        } catch (Exception e) {
            logger.error("Error occurred while processing the payment succeeded event", e);
        }
    }


    public Date increaseSubscription(String userSub, int Days) {
        Payments payment = paymentsService.getPayment(userSub);
        if (payment != null) {
            Date subscriptionDate = payment.getSubscriptionDate();
            if (subscriptionDate == null) {
                subscriptionDate = new Date();
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(subscriptionDate);
            calendar.add(Calendar.DATE, Days);
            subscriptionDate = calendar.getTime();
            return paymentsService.updatePayment(userSub, subscriptionDate).getSubscriptionDate();
        }
        return null;
    }

    public Integer getSubscriptionDaysByAmount(String amount) {
        switch (amount) {
            case "1000":
                return 30;
            case "2500":
                return 90;
            case "4500":
                return 180;
            case "8000":
                return 365;
            default:
                return 0;
        }
    }
        
        
    
}
