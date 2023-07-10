package com.example.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class Employee implements Serializable {
    private static final long serialVersionUID = 1L;

    private  Long id;
    private  String name;
    private String username;
    //不显示密码
    private String password;
    private String phone;
    private String sex;
    //身份证号码
    private String idNumber;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT)
    private Long createUser;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateUser;
}
