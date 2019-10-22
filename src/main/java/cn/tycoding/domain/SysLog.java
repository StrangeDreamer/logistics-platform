package cn.tycoding.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;

@Data
public class SysLog implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private String id;

    private String username; //用户名

    private String operation; //操作

    private String method; //方法名

    private String uri;

    private String params; //参数

    private String ip; //ip地址


    @CreatedDate
    private Date createDate; //操作时间
}
