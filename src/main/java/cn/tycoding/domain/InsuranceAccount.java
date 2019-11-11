package cn.tycoding.domain;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;

/**
 * 2 * @Author: qlXie
 * 3 * @Date: 2019-07-02 11:06
 * 4
 */

@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
public class InsuranceAccount {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int count;

    // 真正的Id是id和type的组合
    private int id;
    // 担保账户的type取值目前只有truck
    private String type = "承运方";

    @CreatedDate
    @Column(updatable = false, nullable = false)
    private Date createdDate;

    @LastModifiedDate
    @Column(updatable = false, nullable = false)
    private Date modifyTime;

    // 承运方的担保额
    private double Money ;
    // 承运方去除冻结、实际可用的担保额
    private double availableMoney ;

    @Lob
    @Column(columnDefinition="TEXT")
    private String InsuranceAccountLog = "\n\n担保账户资金流水：\n";
}

