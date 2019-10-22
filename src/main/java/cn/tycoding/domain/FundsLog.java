package cn.tycoding.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import java.io.Serializable;
@Data
public class FundsLog implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private String id;
    //收款人
    private String moneyReceiver;
    //金额,增加用+，减少用-
    private String amount;
    //原因
    private String moneyRemarks;

}
