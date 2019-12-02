package com.moon.zksample.domain.po;

import lombok.Data;

import javax.persistence.*;

@Data
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "age")
    private Integer age;

    @Column(name = "card_no")
    private Integer cardNo;

    @Column(name = "birthday")
    private String birthday;

    @Column(name = "r_id")
    private Integer rId;
}
