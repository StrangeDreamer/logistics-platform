package cn.tycoding.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.util.Date;
/**
 * 2 * @Author: qlXie
 * 3 * @Date: 2019-07-02 10:53
 * 4
 */

@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
public class BankAccount {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int count;
    // 真正的Id是id和type的组合
    private int id;
    // type取值为：truck shipper platform
    //（platform认为只有一个，其id固定为1）
    private String type;

    @CreatedDate
    @Column(updatable = false, nullable = false)
    private Date createdDate ;

    @LastModifiedDate
    @Column(updatable = false, nullable = false)
    private Date modifyTime;

    // 参与方的资金
    private double money = 100000;

    // 除去冻结实际可用资金
    private double availableMoney = 100000;

    private double yearIncome=0;
    private double monIncome=0;
    private double dayIncome=0;

    // 账户自己的资金流水；这里不允许平台保存其他用户的资金流通（因为资金流通都会经过平台，所以间接保存了所有资金流通）
    private String BankAccountLog = "银行账户资金流水：\n参与方初始资金为100000";
}