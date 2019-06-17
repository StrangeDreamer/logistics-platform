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
    private double originFare;
    //转单更新
    private int originCargoId=-1;

    // 赔偿金，是订单超时要进行赔偿的依据，也是对车辆担保额的要求
    private int insurance;
    // 当前挂单的运费
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
    private double bidPrice = -1;


    /**
     *
     * 0 订单创建未发布 --创建订单
     *
     * 1 订单发布中 --平台确定开抢
     *
     * 2 已接未运 --平台确定最终truck抢单成功
     *
     * 3 已经在运
     *
     * 4 已运达等待收货
     *
     * 5 转手中（正挂在平台公告栏上）--转单成功
     *
     * 6 发布时无人接单撤单 --撤单
     *
     * 7 已接未运撤单 --撤单
     *
     * 8 订单完结 --确定收单时
     *
     * 9 订单超时 --确定收单时
     *
     * 10 订单异常 --确定收单时
     */
    private int status = 0;

}
