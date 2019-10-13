package cn.tycoding.domain;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.Date;


@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Shipper {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int id;
    @NotEmpty
    private String name;

    @NotEmpty
    private String password;
    @CreatedDate
    @Column(updatable = false, nullable = false)
    private Date createdDate ;

    // 发货方评级
    private double ranking = 6;

    @LastModifiedDate
    @Column(updatable = false, nullable = false)
    private Date modifyTime;



    private boolean activated = false;

    // 以下字段仅用于保存，不会被使用
    // 发货方银行账户
    private String bankId;
    private String idgerenshenfenzheng = "0";
    private String id_gongsitongyidaima = "0";
    private String occupation = "0";
    private String telNumber = "0";
    private String address = "经营地址";


}
