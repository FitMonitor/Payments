package deti.fitmonitor.payments.services;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import deti.fitmonitor.payments.models.Payments;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Calendar;
import java.util.Date;

@ExtendWith(MockitoExtension.class)
class StripeWebhookServiceTest {

    @Mock
    private PaymentsService paymentsService;

    @InjectMocks
    private StripeWebhookService stripeWebhookService;

    private String generateMockEvent(String amount, String userSub) {
        return "{"
                + "\"data\": {"
                + "    \"object\": {"
                + "        \"amount_total\": \"" + amount + "\","
                + "        \"metadata\": {"
                + "            \"userSub\": \"" + userSub + "\""
                + "        }"
                + "    }"
                + "}}";
    }

    @Test
    void testHandlePaymentSucceededEvent_withValidEvent() {
        String userSub = "user123";
        String amount = "1000";
        String event = generateMockEvent(amount, userSub);

        Payments mockPayment = new Payments();
        mockPayment.setUserSub(userSub);

        when(paymentsService.getPayment(userSub)).thenReturn(mockPayment);
        when(paymentsService.savePayment(mockPayment)).thenReturn(mockPayment);
        when(paymentsService.updatePayment(eq(userSub), any(Date.class))).thenAnswer(invocation -> {
            Date updatedDate = invocation.getArgument(1);
            mockPayment.setSubscriptionDate(updatedDate);
            return mockPayment;
        });

        stripeWebhookService.handlePaymentSucceededEvent(event);

        verify(paymentsService, times(2)).getPayment(userSub);
        verify(paymentsService, times(1)).savePayment(mockPayment);
        verify(paymentsService, times(1)).updatePayment(eq(userSub), any(Date.class));

        assertNotNull(mockPayment.getSubscriptionDate());
    }

    @Test
    void testHandlePaymentSucceededEvent_withInvalidJson() {
        String event = "INVALID_JSON";

        stripeWebhookService.handlePaymentSucceededEvent(event);

        verifyNoInteractions(paymentsService);
    }

    @Test
    void testIncreaseSubscription_withExistingPayment() {
        String userSub = "user123";
        int days = 30;

        Payments mockPayment = new Payments();
        mockPayment.setUserSub(userSub);
        mockPayment.setSubscriptionDate(new Date());

        when(paymentsService.getPayment(userSub)).thenReturn(mockPayment);
        when(paymentsService.updatePayment(eq(userSub), any(Date.class))).thenAnswer(invocation -> {
            Date updatedDate = invocation.getArgument(1);
            mockPayment.setSubscriptionDate(updatedDate);
            return mockPayment;
        });

        Date result = stripeWebhookService.increaseSubscription(userSub, days);

        assertNotNull(result);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DATE, days);
        assertEquals(calendar.getTime().toString(), result.toString());

        verify(paymentsService, times(1)).getPayment(userSub);
        verify(paymentsService, times(1)).updatePayment(eq(userSub), any(Date.class));
    }

    @Test
    void testIncreaseSubscription_withNoExistingPayment() {
        String userSub = "user123";
        int days = 30;

        when(paymentsService.getPayment(userSub)).thenReturn(null);

        Date result = stripeWebhookService.increaseSubscription(userSub, days);

        assertNull(result);
        verify(paymentsService, times(1)).getPayment(userSub);
        verify(paymentsService, times(0)).updatePayment(anyString(), any(Date.class));
    }

    @Test
    void testGetSubscriptionDaysByAmount() {
        assertEquals(30, stripeWebhookService.getSubscriptionDaysByAmount("1000"));
        assertEquals(90, stripeWebhookService.getSubscriptionDaysByAmount("2500"));
        assertEquals(180, stripeWebhookService.getSubscriptionDaysByAmount("4500"));
        assertEquals(365, stripeWebhookService.getSubscriptionDaysByAmount("8000"));
        assertEquals(0, stripeWebhookService.getSubscriptionDaysByAmount("9999"));
    }
}
