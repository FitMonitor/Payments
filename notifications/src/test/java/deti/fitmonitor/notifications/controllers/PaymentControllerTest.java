package deti.fitmonitor.notifications.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import deti.fitmonitor.notifications.models.Payments;
import deti.fitmonitor.notifications.services.PaymentsService;
import deti.fitmonitor.notifications.services.StripeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@WebMvcTest(PaymentController.class)
@AutoConfigureMockMvc(addFilters = false)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StripeService stripeService;

    @MockBean
    private PaymentsService paymentsService;


    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void testCreateCheckoutSession() throws Exception {
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("amount", 5000L);
        requestData.put("currency", "usd");
        requestData.put("successUrl", "http://localhost/success");
        requestData.put("cancelUrl", "http://localhost/cancel");

        Map<String, String> mockResponse = new HashMap<>();
        mockResponse.put("url", "https://checkout.stripe.com/pay/cs_test_example");

        when(stripeService.createCheckoutSession(any(), any(), any(), any(),any())).thenReturn(mockResponse);

        mockMvc.perform(post("/api/payment/create-checkout-session")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("https://checkout.stripe.com/pay/cs_test_example"));

        verify(stripeService, times(1)).createCheckoutSession(any(), any(), any(), any(),any());
    }

    @Test
    void testCreateCheckoutSession_withMissingFields() throws Exception {


        mockMvc.perform(post("/api/payment/create-checkout-session")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(value = "user123")
    void testGetSubscriptionDate() throws Exception {
        String userSub = "user123";

        Payments old_payment = new Payments();
        old_payment.setUserSub(userSub);

        Payments mockPayment = new Payments();
        mockPayment.setUserSub(userSub);
        mockPayment.setSubscriptionDate(new Date());


        System.out.println("ahhh"+mockPayment.getSubscriptionDate());

        when(paymentsService.getPayment(userSub)).thenReturn(mockPayment);

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(userSub, null));


        mockMvc.perform(get("/api/payment/user/subscriptiondate")
                .requestAttr("userSub", userSub))
                .andExpect(content().string(mockPayment.getSubscriptionDate().toString()));

        verify(paymentsService, times(1)).getPayment(userSub);
    }

    @Test
    @WithMockUser(username = "user123")
    void testGetSubscriptionDateNotFound() throws Exception {
        String userSub = "user123";

        when(paymentsService.getPayment(userSub)).thenReturn(null);

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(userSub, null));



        mockMvc.perform(get("/api/payment/user/subscriptiondate"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
                

        verify(paymentsService, times(1)).getPayment(userSub);
    }
}
