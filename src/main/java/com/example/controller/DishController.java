package com.example.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.CustomException;
import com.example.common.R;
import com.example.dto.DishDto;
import com.example.entity.Category;
import com.example.entity.Dish;
import com.example.entity.DishFlavor;
import com.example.entity.Setmeal;
import com.example.service.CategoryService;
import com.example.service.DishFlavorService;
import com.example.service.DishService;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dish")
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;


    //新增菜品
    //requestbody 封装从客户端传来的json数据
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        dishService.saveWithFlavor(dishDto);
        return R.success("新增菜品成功");
    }

    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, HttpServletRequest request,String name){

        //分页构造器
        Page<Dish> pageInfo = new Page<>(page,pageSize);
        Page<DishDto> dishDtoPage = new Page<>();
        //条件构造器
        LambdaQueryWrapper<Dish> qw = new LambdaQueryWrapper();
        //添加过滤条件
        qw.like(Strings.isNotEmpty(name),Dish::getName,name);
        //添加排序条件
        qw.orderByDesc(Dish::getUpdateTime);
        //执行分页查询
        dishService.page(pageInfo,qw);

        //对象拷贝
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");

        List<Dish> records = pageInfo.getRecords();

        List<DishDto> list =  records.stream().map((item) -> {
            DishDto dishDto = new DishDto();

            BeanUtils.copyProperties(item,dishDto);
            //获取分类id
            Long categoryId = item.getCategoryId();
            //查询分类对象
            Category category = categoryService.getById(categoryId);
            if (category != null){
                //id对应的分类的名称
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            return dishDto;

        })
                //转换成集合
                .collect(Collectors.toList());


        dishDtoPage.setRecords(list);

        return R.success(dishDtoPage);
    }

    //数据回显
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){

        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);

    }


    //修改后菜品保存
    //requestbody 封装从客户端传来的json数据
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        dishService.updateWithFlavor(dishDto);
        return R.success("新增菜品成功");
    }

//    //根据条件查询对应的菜品数据
//    @GetMapping("/list")
//    public R<List<Dish>> list(Dish dish){
//        LambdaQueryWrapper<Dish> qw = new LambdaQueryWrapper<>();
//        qw.eq(dish.getCategoryId() != null,Dish::getCategoryId,dish.getCategoryId());
//
//        //查询状态是1的，也就是在售的
//        qw.eq(Dish::getStatus,1);
//        //添加排序条件
//        qw.orderByDesc(Dish::getSort).orderByAsc(Dish::getUpdateTime);
//        List<Dish> list = dishService.list(qw);
//        return R.success(list);
//    }


    //根据条件查询对应的菜品数据
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){
        LambdaQueryWrapper<Dish> qw = new LambdaQueryWrapper<>();
        qw.eq(dish.getCategoryId() != null,Dish::getCategoryId,dish.getCategoryId());

        //查询状态是1的，也就是在售的
        qw.eq(Dish::getStatus,1);
        //添加排序条件
        qw.orderByDesc(Dish::getSort).orderByAsc(Dish::getUpdateTime);
        List<Dish> list = dishService.list(qw);

        List<DishDto> dishDtoList =  list.stream().map((item) -> {
            DishDto dishDto = new DishDto();

            BeanUtils.copyProperties(item,dishDto);
            //获取分类id
            Long categoryId = item.getCategoryId();
            //查询分类对象
            Category category = categoryService.getById(categoryId);
            if (category != null){
                //id对应的分类的名称
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }

            //当前菜品的ID
            Long dishId = item.getId();
            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(DishFlavor::getDishId,dishId);
            List<DishFlavor> dishFlavorList = dishFlavorService.list(lambdaQueryWrapper);
            dishDto.setFlavors(dishFlavorList);
            return dishDto;

        })
                //转换成集合
                .collect(Collectors.toList());

        return R.success(dishDtoList);
    }

    @DeleteMapping
    public R<String> delete(@RequestParam("ids") List<Long> ids){
        ids.stream().forEach((item) -> {
            LambdaQueryWrapper<Dish> qw = new LambdaQueryWrapper<>();
            qw.eq(item != null,Dish::getId,item);
            Dish dish = dishService.getById(item);
            dishService.removeById(dish);
        });
        return R.success("删除成功");
    }

    @PostMapping("/status/{status}")
    @Transactional
    public R<String> updateStatus(@RequestParam List<Long> ids,@PathVariable("status") Integer status){

        ids.stream().forEach((item) -> {
            LambdaQueryWrapper<Dish> qw = new LambdaQueryWrapper<>();
            qw.eq(item != null,Dish::getId,item);
            Dish dish = dishService.getById(item);
            dish.setStatus(status);
            dishService.updateById(dish);

        });
        return R.success("更新商品售卖状态成功！");
    }

}


