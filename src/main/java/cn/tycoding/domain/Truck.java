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
    private double ranking = 6;

    @CreatedDate
    @Column(updatable = false, nullable = false)
    private Date createdDate ;

    @LastModifiedDate
    @Column(updatable = false, nullable = false)
    private Date modifyTime;


    // 当前所在地点，会随着不断的接口调用从而不断被刷新
    private String position;


    private boolean activated = false;

    // 以下字段仅用于保存，不会被使用
    // 承运方的银行id
    private String bankId;
    // 承运方的保险账户
    private String insuranceId;
    private int power = 0;
    private String idgerenshenfenzheng = "0";
    private String id_gongsitongyidaima = "0";
    private String id_xingshizheng = "0";
    private String id_jiashizheng = "0";
    private String telNumber = "0";
    private String address = "经营地址";

    // 订单发布范围：在平台发布订单的时候，只会对同样在该范围的承运方进行发布
    private String field = "默认";
}
