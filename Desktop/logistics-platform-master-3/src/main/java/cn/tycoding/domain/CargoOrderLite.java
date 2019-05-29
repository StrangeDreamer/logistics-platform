package cn.tycoding.domain;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.math.BigDecimal;

@Data
@Entity
public class CargoOrderLite {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int id;
    private int cargoId;
    private BigDecimal costPrice;
    private int truckId;
}
