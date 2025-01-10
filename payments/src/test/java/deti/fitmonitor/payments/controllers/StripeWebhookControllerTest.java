package deti.fitmonitor.payments.controllers;

import com.stripe.model.Event;
import com.stripe.net.Webhook;
import deti.fitmonitor.payments.services.StripeWebhookService;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StripeWebhookController.class)
@AutoConfigureMockMvc(addFilters = false)
class StripeWebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StripeWebhookService stripeWebhookService;

    @Value("${stripe.webhook.secret}")
    private String endpointSecret;

    @Test
    void whenHandleWebhookWithValidPayload_thenReturnStatusOk() throws Exception {
        String payload = "{\"id\": \"evt_1\", \"type\": \"checkout.session.completed\"}";
        String sigHeader = "valid_signature";

        try (MockedStatic<Webhook> webhookMock = mockStatic(Webhook.class)) {
            Event mockEvent = new Event();
            mockEvent.setId("evt_1");
            mockEvent.setType("checkout.session.completed");

            webhookMock.when(() -> Webhook.constructEvent(payload, sigHeader, endpointSecret))
                    .thenReturn(mockEvent);

            mockMvc.perform(post("/default/api/payments/webhooks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload)
                            .header("Stripe-Signature", sigHeader))
                    .andExpect(status().isOk());

            webhookMock.verify(() -> Webhook.constructEvent(payload, sigHeader, endpointSecret), times(1));
        }
    }
}

