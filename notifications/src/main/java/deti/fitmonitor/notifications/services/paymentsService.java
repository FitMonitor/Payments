package deti.fitmonitor.notifications.services;

import java.util.Date;

import org.springframework.stereotype.Service;
import deti.fitmonitor.notifications.models.Payments;
import deti.fitmonitor.notifications.repositories.paymentsRepository;

@Service
public class paymentsService {

    private final paymentsRepository paymentsRepository;

    public paymentsService(paymentsRepository paymentsRepository) {
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
