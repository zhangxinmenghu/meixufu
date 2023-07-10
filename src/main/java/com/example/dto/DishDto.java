package com.example.dto;

import com.example.entity.Dish;
import com.example.entity.DishFlavor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

//dto 数据传输对象
@Data
public class DishDto extends Dish {

    //菜品对应的口味数据
    private List<DishFlavor> flavors = new ArrayList<>();

    private String categoryName;

    private Integer copies;
}
