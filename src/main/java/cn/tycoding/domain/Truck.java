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
    private double rank = 6;

    @CreatedDate
    @Column(updatable = false, nullable = false)
    private Date createdDate ;

    @LastModifiedDate
    @Column(updatable = false, nullable = false)
    private Date modifyTime;

    // 承运方的银行id
    private long bankId;
    // 承运方的保险账户
    private long insuranceId;

    private boolean activated = false;

    // 以下字段仅用于保存，不会被使用
    private int power = 0;
    private long id_gerenshenfenzheng = 0;
    private long id_gongsitongyidaima = 0;
    private long id_xingshizheng = 0;
    private long id_jiashizheng = 0;
    private String telNumber = "";
}
