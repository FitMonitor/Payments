package deti.fitmonitor.payments.services;

import java.util.Date;

import org.springframework.stereotype.Service;
import deti.fitmonitor.payments.models.Payments;
import deti.fitmonitor.payments.repositories.paymentsRepository;

@Service
public class PaymentsService {

    private final paymentsRepository paymentsRepository;

    public PaymentsService(paymentsRepository paymentsRepository) {
        this.paymentsRepository = paymentsRepository;
    }

    public Payments getPayment(String userSub) {
        return paymentsRepository.findByUserSub(userSub).orElse(null);
    }

    public Payments savePayment(Payments payment) {
        return paymentsRepository.save(payment);
    }

    public void deletePayment(String userSub) {
        paymentsRepository.deleteById(userSub);
    }

    public Payments updatePayment(String userSub, Date subscriptionDate) {
        Payments payment = paymentsRepository.findByUserSub(userSub).orElse(null);
        if (payment != null) {
            payment.setSubscriptionDate(subscriptionDate);
            return paymentsRepository.save(payment);
        }
        return null;
    }


    
}
