package cn.tycoding.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Receiver {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int id;
    private String name;

    @CreatedDate
    @Column(updatable = false, nullable = false)
    private Date createdDate ;

    @LastModifiedDate
    @Column(updatable = false, nullable = false)
    private Date modifyTime;


    // 以下字段仅用于保存，不会被使用
    private String idgerenshenfenzheng = "0";
    private String id_gongsitongyidaima = "0";
    private String occupation = "0";
    private String telNumber = "0";
}
