package cn.tycoding.dto;


import lombok.Data;
import org.springframework.data.annotation.CreatedDate;



import java.util.Date;

/**
 * TODO 转单可更改信息
 */

@Data
public class CargoInfoChangeDTO {


    private double freightFare;
    private int receiverId;

    @CreatedDate
    private Date createTime;




}
