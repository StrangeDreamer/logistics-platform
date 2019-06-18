package cn.tycoding.domain;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * @auther qlXie
 * @date 2019-06-11 10:08
 */

@Data
@Entity
@Table(name = "platform")
@EntityListeners(AuditingEntityListener.class)
public class Platform implements Serializable {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int id;

    @CreatedDate
    @Column(updatable = false,nullable = false)
    private Date createTime;

    private double lowestBidPriceRatio = 0.4;
    private double shipperProfitRatio = 0.2;
    private double platformProfitRatio = 0.3;
    private double truckProfitRatio = 0.5;
    private double withdrawFeeRatio = 0.2;
    private double overTimeFeeRatio = 0.05;

    private int exhibitionFee = 10;



//    // 平台当前的资金
//    private double lmoney = 0;

}
