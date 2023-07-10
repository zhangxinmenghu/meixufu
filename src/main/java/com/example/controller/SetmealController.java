package com.example.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.R;
import com.example.dto.SetmealDto;
import com.example.entity.Category;
import com.example.entity.Dish;
import com.example.entity.Setmeal;
import com.example.entity.SetmealDish;
import com.example.service.CategoryService;
import com.example.service.SetmealDishService;
import com.example.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import sun.dc.pr.PRError;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {
    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private CategoryService categoryService;

    //新增套餐
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto){
        setmealService.saveWithDish(setmealDto);
        return R.success("新增套餐成功");
    }


    //套餐分页查询
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        Page<Setmeal> pageInfo = new Page<>(page,pageSize);
        Page<SetmealDto> dtoPage = new Page<>();


        LambdaQueryWrapper<Setmeal> qw = new LambdaQueryWrapper<>();
        qw.like(Strings.isNotEmpty(name),Setmeal::getName,name);
        //排序条件
        qw.orderByDesc(Setmeal::getUpdateTime);

        setmealService.page(pageInfo,qw);

        //对象拷贝
        BeanUtils.copyProperties(pageInfo,dtoPage,"records");

        List<Setmeal> records = pageInfo.getRecords();

        List<SetmealDto> list = records.stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item,setmealDto);
            //分类id
            Long categoryId = item.getCategoryId();
            //根据分类id查找分类对象
            Category category = categoryService.getById(categoryId);
            if (category != null){
                String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);
            }
            return setmealDto;

        }).collect(Collectors.toList());


        dtoPage.setRecords(list);
        return R.success(dtoPage);
    }

    //删除套餐
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){
        setmealService.deleteWithDish(ids);
        return R.success("删除成功");
    }

    @PostMapping("/status/{status}")
    public R<String> updateStatus(@RequestParam List<Long> ids,@PathVariable("status") Integer status){

        ids.stream().forEach((item) -> {
            LambdaQueryWrapper<Dish> qw = new LambdaQueryWrapper<>();
            qw.eq(item != null,Dish::getId,item);
            Setmeal setmeal = setmealService.getById(item);
            setmeal.setStatus(status);
            setmealService.updateById(setmeal);

        });
        return R.success("更新商品状态成功！");
    }


    //根据条件查询套餐数据
    //json用@requestbody
    //键值对不需要
    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal){

        LambdaQueryWrapper<Setmeal> qw = new LambdaQueryWrapper<>();
        qw.eq(setmeal.getCategoryId() != null,Setmeal::getCategoryId,setmeal.getCategoryId());
        qw.eq(setmeal.getStatus() != null,Setmeal::getStatus,setmeal.getStatus());
        qw.orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal> list = setmealService.list(qw);


        return R.success(list);
    }
}
