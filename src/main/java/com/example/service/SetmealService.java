package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.dto.SetmealDto;
import com.example.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    //新增套餐，同时关联套餐表和套餐菜品关系表
    public void saveWithDish(SetmealDto setmealDto);

    //删除套餐,同时删除套餐和菜品的关联数据
    public void deleteWithDish(List<Long> ids);
}
