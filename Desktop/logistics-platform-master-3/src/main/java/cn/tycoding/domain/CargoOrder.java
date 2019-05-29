package cn.tycoding.domain;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Entity
public class CargoOrder {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int id;
    private int cargoId;
    private BigDecimal costPrice;
    private int truckId;
    private boolean isOvertime = false;
    private boolean isAbnormal = false;
    private double orderPrice;
    private int status;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date registerTime;
    private Date completeTime;
}
