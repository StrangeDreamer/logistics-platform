package cn.tycoding.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Shipper {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int id;
    private String name;

    @CreatedDate
    @Column(updatable = false, nullable = false)
    private Date createdDate ;

    @LastModifiedDate
    @Column(updatable = false, nullable = false)
    private Date modifyTime;


//    // 发货方的资金
//    private double money = 0;
//
//    // 发货方冻结后实际可用资金
//    private double availableMoney = 0;


    // 发货方银行账户
    private long BankId;
}
