package deti.fitmonitor.payments.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "payments")
public class Payments {

    @Id
    private String userSub;

    @Column(name = "subscription_date")
    private Date subscriptionDate;
    
}
