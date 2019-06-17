package cn.tycoding.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Truck {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int id;
    private String name;
    private double availableWeight;
    private double availableVolume;
    private String type;

    @CreatedDate
    @Column(updatable = false, nullable = false)
    private Date createdDate ;

    @LastModifiedDate
    @Column(updatable = false, nullable = false)
    private Date modifyTime;


//    // 承运方的资金
//    private double money = 0;
//
//    // 承运方冻结后实际可用资金
//    private double availableMoney = 0;
//
//    // 承运方的担保额
//    private double insurance = 0;
//    // 承运方去除冻结、实际可用的担保额
//    private double availableInsurance = 0;


    // 承运方的银行id
    private int bankId;
    // 承运方的保险账户
    private int insuranceId;
}
