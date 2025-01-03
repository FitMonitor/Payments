package deti.fitmonitor.notifications.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Date;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import deti.fitmonitor.notifications.models.Payments;
import deti.fitmonitor.notifications.repositories.paymentsRepository;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentsServiceTest {

    @Mock
    private paymentsRepository paymentsRepository;

    @InjectMocks
    private PaymentsService paymentsService;


    @Test
    void testGetPaymentExists() {
        String userSub = "user123";
        Payments payment = new Payments();
        payment.setUserSub(userSub);
        payment.setSubscriptionDate(new Date());

        when(paymentsRepository.findByUserSub(userSub)).thenReturn(Optional.of(payment));

        Payments result = paymentsService.getPayment(userSub);

        assertNotNull(result);
        assertEquals(userSub, result.getUserSub());
        verify(paymentsRepository, times(1)).findByUserSub(userSub);
    }

    @Test
    void testGetPaymentNotExists() {
        String userSub = "user123";

        when(paymentsRepository.findByUserSub(userSub)).thenReturn(Optional.empty());

        Payments result = paymentsService.getPayment(userSub);

        assertNull(result);
        verify(paymentsRepository, times(1)).findByUserSub(userSub);
    }

    @Test
    void testSavePayment() {
        Payments payment = new Payments();
        payment.setUserSub("user123");
        payment.setSubscriptionDate(new Date());

        when(paymentsRepository.save(payment)).thenReturn(payment);

        Payments result = paymentsService.savePayment(payment);

        assertNotNull(result);
        assertEquals("user123", result.getUserSub());
        verify(paymentsRepository, times(1)).save(payment);
    }

    @Test
    void testDeletePayment() {
        String userSub = "user123";

        doNothing().when(paymentsRepository).deleteById(userSub);

        paymentsService.deletePayment(userSub);

        verify(paymentsRepository, times(1)).deleteById(userSub);
    }

    @Test
    void testUpdatePaymentExists() {
        String userSub = "user123";
        Date subscriptionDate = new Date();
        Payments payment = new Payments();
        payment.setUserSub(userSub);

        when(paymentsRepository.findByUserSub(userSub)).thenReturn(Optional.of(payment));
        when(paymentsRepository.save(payment)).thenReturn(payment);

        Payments result = paymentsService.updatePayment(userSub, subscriptionDate);

        assertNotNull(result);
        assertEquals(subscriptionDate, result.getSubscriptionDate());
        verify(paymentsRepository, times(1)).findByUserSub(userSub);
        verify(paymentsRepository, times(1)).save(payment);
    }

    @Test
    void testUpdatePaymentNotExists() {
        String userSub = "user123";
        Date subscriptionDate = new Date();

        when(paymentsRepository.findByUserSub(userSub)).thenReturn(Optional.empty());

        Payments result = paymentsService.updatePayment(userSub, subscriptionDate);

        assertNull(result);
        verify(paymentsRepository, times(1)).findByUserSub(userSub);
        verify(paymentsRepository, times(0)).save(any(Payments.class));
    }
}
