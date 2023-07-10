package com.example.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.CustomException;
import com.example.dto.SetmealDto;
import com.example.entity.Setmeal;
import com.example.entity.SetmealDish;
import com.example.mapper.SetmealMapper;
import com.example.service.SetmealDishService;
import com.example.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    @Transactional
    @Override
    public void saveWithDish(SetmealDto setmealDto) {
        //保存套餐基本信息，操作setmeal，insert操作
        this.save(setmealDto);

        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes.stream().map((item) -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        //保存套餐和菜品的关联信息，操作setmeal_dish，insert操作
        setmealDishService.saveBatch(setmealDishes);

    }

    @Override
    //操作两张表要加事务注解
    @Transactional
    public void deleteWithDish(List<Long> ids) {
        //查询套餐状态，查看是否可以删除，如果不能删除则抛出业务异常信息
        LambdaQueryWrapper<Setmeal> qw = new LambdaQueryWrapper<>();
        qw.in(Setmeal::getId,ids);
        qw.eq(Setmeal::getStatus,1);
        int count = this.count(qw);
        if (count > 0){
            throw new CustomException("套餐正在售卖中，不能删除");
        }
        //如果可以删除，先删除套餐表中的数据--setmeal
        this.removeByIds(ids);
        //删除关系表中的数据--setmealDish
        LambdaQueryWrapper<SetmealDish> sdqw = new LambdaQueryWrapper<>();
        sdqw.in(SetmealDish::getSetmealId,ids);
        setmealDishService.remove(sdqw);
    }
}
