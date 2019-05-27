package cn.tycoding.domain;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
public class Reciever {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int id;

    private String name;

}
