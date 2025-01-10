package deti.fitmonitor.payments.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import deti.fitmonitor.payments.models.Payments;

@Repository
public interface paymentsRepository extends JpaRepository<Payments, String> {
    Optional<Payments> findByUserSub(String userSub);
    
}
