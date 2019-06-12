package cn.tycoding.domain;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;

/**
 * @auther qlXie
 * @date 2019-06-12 10:52
 */
@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Inspection {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private int id;
    private int cargoId;
    // 对应cargo的status
    private int inspectionResult;
    // 超时时长 仅在inspectionResult为的时候才会起作用
    private int timeoutPeriod = 0;

    @CreatedDate
    @Column(updatable = false, nullable = false)
    private Date createdTime ;

}
