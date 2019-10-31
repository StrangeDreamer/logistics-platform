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
    private double preFare;
    //转单更新
    private Integer preCargoId;

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

    // 当前所在地点，会随着不断的接口调用从而不断被刷新
    private String position;

    // 订单完成度
    private double completeRatio = 0;

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
     * 5 转手中（原订单的属性，表示其产生的新订单正挂在平台公告栏上）
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
     *
     * 11 被正常转单，本订单已经失效
     *
     * 12 该订单为第三类撤单产生的返程订单
     *
     * 13 验收超时
     *
     * 14 提醒验货时刻
     *
     * 15 承运方拒单
     */
    private int status = 0;

    private String remarks = "";

    // 订单发布范围：在平台发布订单的时候，只会对同样在该范围的承运方进行发布
    private String field = "全体承运方";

    // 货物状态变更日志；目前只记录验货状态，如果后面需要其他状态日志，可以加上去
    @Lob
    @Column(columnDefinition="TEXT")
    private String cargoStatusLog = "目前货物尚未运达";


    // 该订单作为转单订单时，其发货承运方的名称
    private String transCargoName = "非转手订单";
}
