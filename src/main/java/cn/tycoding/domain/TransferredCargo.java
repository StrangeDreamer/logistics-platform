package cn.tycoding.domain;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
public class TransferredCargo {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int id;
    private int cargoId;
    //private int TransferredTruckId;

    private double freightFare;

    @CreatedDate
    @Column(updatable = false, nullable = false)
    private Date createdTime ;

    //    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date bidStartTime;

    //    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date bidEndTime;


    // order属性
    private int truckId = -1;
    private boolean overtime = false;
    private boolean abnormal = false;
    private double bidPrice = -1;
    private int status = 0;

}
