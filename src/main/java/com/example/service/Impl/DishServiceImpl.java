package com.example.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dto.DishDto;
import com.example.entity.Dish;
import com.example.entity.DishFlavor;
import com.example.mapper.DishMapper;
import com.example.service.DishFlavorService;
import com.example.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {


    @Autowired
    private DishFlavorService dishFlavorService;
    //新增菜品，同时保存对应的口味数据
    @Override
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        //保存菜品的基本信息到菜品表dish
        this.save(dishDto);

        //菜品id
        Long dishId = dishDto.getId();

        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());


        //保存菜品口味数据到菜品口味表dishflavor
        dishFlavorService.saveBatch(flavors);

    }


    //数据回显
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        //从菜品表查询
        Dish dish = this.getById(id);

        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish,dishDto);
        //从口味表查询
        LambdaQueryWrapper<DishFlavor> qw = new LambdaQueryWrapper<DishFlavor>();
        qw.eq(DishFlavor::getDishId,dish.getId());
        List<DishFlavor> flavors = dishFlavorService.list(qw);
        dishDto.setFlavors(flavors);

        return dishDto;
    }


    @Override
    public void updateWithFlavor(DishDto dishDto) {

        //更新dish表的基本信息
        this.updateById(dishDto);

        //清理菜品对应的口味数据-dish_flavor表的delete操作
        LambdaQueryWrapper<DishFlavor> qw = new LambdaQueryWrapper<>();
        qw.eq(DishFlavor::getDishId,dishDto.getId());
        dishFlavorService.remove(qw);

        //添加提交过来的口味数据,insert操作
        List<DishFlavor> flavors = dishDto.getFlavors();

        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());

        //批量保存
        dishFlavorService.saveBatch(flavors);
    }
}
