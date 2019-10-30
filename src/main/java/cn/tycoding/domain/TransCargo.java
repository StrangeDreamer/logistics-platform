package cn.tycoding.domain;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import javax.persistence.*;
import java.io.Serializable;

import java.util.Date;

@Data
@Entity
@Table(name = "transCargo")
@EntityListeners(AuditingEntityListener.class)
public class TransCargo implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int id;

    private int cargoId;
    // 转手订单的直接发货方 （真实身份为承运方)
    private int directShipperId;
    // 转手订单的直接发货承运方名称
    private String directShipperName;

    @CreatedDate
    @Column(updatable = false, nullable = false)
    private Date createdTime ;

}
