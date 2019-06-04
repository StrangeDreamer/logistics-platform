package cn.tycoding.domain;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

@Data
@Entity
@Table(name = "cargo")
@EntityListeners(AuditingEntityListener.class)
public class Cargo implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int id;
    private int shipperId;
    private double freightFare;
    private int receiverId;
    private double weight;
    private double volume;
    private String type;

    @CreatedDate
    @Column(updatable = false, nullable = false)
    private Date createdTime ;

//    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date limitedTime;

//    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date bidStartTime;

//    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date bidEndTime;



    // 出发地、目的地，数据类型可能要看地图的要求
    private String departure;
    private String destination;


    // order属性
    private int truckId = -1;
    private boolean overtime = false;
    private boolean abnormal = false;
    private double orderPrice=-1;
    private int status = 0;



}
