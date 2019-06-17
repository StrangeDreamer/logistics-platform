package cn.tycoding.domain;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Bid {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int id;
    private int cargoId;
    private double bidPrice;
    private int truckId;

    @CreatedDate
    @Column(updatable = false, nullable = false)
    private Date createdTime ;

}
