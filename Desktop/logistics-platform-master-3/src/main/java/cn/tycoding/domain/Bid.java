package cn.tycoding.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;
@Data
@Entity
// 出价信息，由承运方发送，平台解析处理。
public class Bid {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private int id;
    private int truckId;
    private int cargoId;
    private double bidingMoney;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date biddingTime;
}
