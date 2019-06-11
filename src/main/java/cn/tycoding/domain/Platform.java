package cn.tycoding.domain;

import lombok.Data;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;

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

    // 平台当前的资金
    private double lmoney = 0;

}
