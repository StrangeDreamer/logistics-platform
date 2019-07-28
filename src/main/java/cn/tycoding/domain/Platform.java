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

    // 已接未运的
    private double withdrawFeeRatio = 0.2;

    // 超时起步惩罚占实际运费的比例
    private double overTimeFeeRatio = 0.05;

    // 红包上限，如果实际红包高于该比例与运费的乘积，则以乘积为最终红包
    private double bonusMaxRatioInFare = 0.05;

    private int exhibitionFee = 500;

    private int bidingDuration = 180;

}
