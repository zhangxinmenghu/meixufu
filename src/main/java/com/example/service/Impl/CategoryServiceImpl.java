package com.example.service.Impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.CustomException;
import com.example.common.R;
import com.example.entity.Category;
import com.example.entity.Dish;
import com.example.entity.Setmeal;
import com.example.mapper.CategoryMapper;
import com.example.service.CategoryService;
import com.example.service.DishService;
import com.example.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private DishService dishService;
    @Override
    public void remove(Long id) {
        LambdaQueryWrapper<Dish> dishQw = new LambdaQueryWrapper<>();
        //根据分类id查询
        dishQw.eq(Dish::getCategoryId,id);
        //查询当前分类是否关联了菜品，如果已经关联，抛出一个业务异常
        int count1 = dishService.count(dishQw);
        if (count1 > 0){
            //已经关联了菜品，抛出业务异常
            throw new CustomException("当前分类下关联了其他菜品,无法删除");
        }

        //查询当前分类是否关联了套餐，如果已经关联，抛出一个业务异常
        LambdaQueryWrapper<Setmeal> setmealQw = new LambdaQueryWrapper<>();
        setmealQw.eq(Setmeal::getCategoryId,id);
        int count2 = setmealService.count(setmealQw);
        if (count2 > 0 ){
            //已经关联了套餐，抛出业务异常
            throw new CustomException("当前分类下关联了其他套餐,无法删除");
        }

        //如果两个条件都不成立，那么可以正常删除分类
        super.removeById(id);
    }
}
