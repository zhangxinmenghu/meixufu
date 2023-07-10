package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.dto.DishDto;
import com.example.entity.Dish;

public interface DishService extends IService<Dish> {
    //新增菜品，同时插入菜品对应的口味数据，需要操作两张表，dish和dishflavor
    public void saveWithFlavor(DishDto dishDto);

    //根据id查询菜品表和口味表
    public DishDto getByIdWithFlavor(Long id);
    //更新菜品和口味
    public void updateWithFlavor(DishDto dishDto);
}
