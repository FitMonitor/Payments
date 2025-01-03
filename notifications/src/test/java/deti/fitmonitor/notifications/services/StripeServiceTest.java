package deti.fitmonitor.notifications.services;

import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StripeServiceTest {

    private final StripeService stripeService = new StripeService();

    @Test
    void test_CreateCheckoutSession_Success() throws Exception {
        ReflectionTestUtils.setField(stripeService, "stripeApiKey", "sk_test_12345");
        Stripe.apiKey = "sk_test_12345";

        try (MockedStatic<Session> sessionMock = mockStatic(Session.class)) {
            Session mockSession = mock(Session.class);
            when(mockSession.getUrl()).thenReturn("https://checkout.stripe.com/session_id");
            sessionMock.when(() -> Session.create(any(SessionCreateParams.class))).thenReturn(mockSession);

            Map<String, String> response = stripeService.createCheckoutSession(5000L, "usd", "http://success.url", "http://cancel.url","user_sub");

            assertNotNull(response);
            assertEquals("https://checkout.stripe.com/session_id", response.get("url"));

            sessionMock.verify(() -> Session.create(any(SessionCreateParams.class)), times(1));
        }
    }

    @Test
    void test_CreateCheckoutSession_Failure() {
        ReflectionTestUtils.setField(stripeService, "stripeApiKey", "sk_test_12345");
        Stripe.apiKey = "sk_test_12345";

        try (MockedStatic<Session> sessionMock = mockStatic(Session.class)) {
            sessionMock.when(() -> Session.create(any(SessionCreateParams.class)))
                    .thenThrow(new RuntimeException("Stripe API error"));

            Exception exception = assertThrows(Exception.class, () ->
                    stripeService.createCheckoutSession(5000L, "usd", "http://success.url", "http://cancel.url","user_sub")
            );

            assertEquals("Error occurred while creating the Stripe checkout session", exception.getMessage());

            sessionMock.verify(() -> Session.create(any(SessionCreateParams.class)), times(1));
        }
    }
}

