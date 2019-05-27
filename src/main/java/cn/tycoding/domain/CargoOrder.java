package cn.tycoding.domain;


import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;

@Data
@Entity
public class CargoOrder {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int id;
    private int cargoId;
    private BigDecimal costPrice;
    private int trunkId;


}
