package deti.fitmonitor.notifications.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import deti.fitmonitor.notifications.models.Payments;

@Repository
public interface paymentsRepository extends JpaRepository<Payments, String> {
    Optional<Payments> findByUserSub(String userSub);
    
}
