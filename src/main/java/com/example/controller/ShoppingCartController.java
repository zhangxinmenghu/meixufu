package com.example.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.common.BaseContext;
import com.example.common.R;
import com.example.entity.ShoppingCart;
import com.example.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 购物车
 */
@Slf4j
@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    //添加到购物车
    @PostMapping("/add")
    public  R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){

        //获取当前用户id，指定购物车数据是哪个用户的
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);


        //查询当前添加的是菜品还是套餐
        Long dishId = shoppingCart.getDishId();

        LambdaQueryWrapper<ShoppingCart> qw = new LambdaQueryWrapper<>();
        qw.eq(ShoppingCart::getUserId,currentId);

        if (dishId != null){
            //添加的是菜品
            qw.eq(ShoppingCart::getDishId,shoppingCart.getDishId());
        }else {
            //添加的是套餐
            qw.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }
        //查询当前菜品是否在购物车中
        //查购物车里的数据，取出一条
        ShoppingCart cartServiceOne = shoppingCartService.getOne(qw);
        if (cartServiceOne != null){
            //已存在
            //用查出来的数据查，不用前端传来的查
            Integer number = cartServiceOne.getNumber();
            log.info(number.toString());
            cartServiceOne.setNumber(number + 1);
            shoppingCartService.updateById(cartServiceOne);
        }else {
            //未存在
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            cartServiceOne =shoppingCart;
        }
        return R.success(cartServiceOne);
    }

    @PostMapping("/sub")
    @Transactional
    public  R<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart){

        //获取当前用户id，指定购物车数据是哪个用户的
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);

        //查询当前添加的是菜品还是套餐
        Long dishId = shoppingCart.getDishId();

        LambdaQueryWrapper<ShoppingCart> qw = new LambdaQueryWrapper<>();
        qw.eq(ShoppingCart::getUserId,currentId);

        if (dishId != null){
            //减少的是菜品
            qw.eq(ShoppingCart::getDishId,shoppingCart.getDishId());
        }else {
            //减少的是套餐
            qw.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }

        //查询出的在购物车数据
        ShoppingCart cartServiceOne = shoppingCartService.getOne(qw);
        if (cartServiceOne != null){
            Integer number = cartServiceOne.getNumber();

            if (number > 1 ){
                cartServiceOne.setNumber(number - 1);
                shoppingCartService.updateById(cartServiceOne);
                return R.success(cartServiceOne);
            }else if (number - 1 <= 0){
                shoppingCartService.removeById(cartServiceOne.getId());
                return R.success(cartServiceOne);
            }
        }

        return R.error("操作异常");
    }


    //查看购物车
    @GetMapping("/list")
    public R< List<ShoppingCart> > list(){

        LambdaQueryWrapper<ShoppingCart> qw = new LambdaQueryWrapper<>();
        qw.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        qw.orderByAsc(ShoppingCart::getCreateTime);
        List<ShoppingCart> list = shoppingCartService.list(qw);

        return R.success(list);

    }


    @DeleteMapping("/clean")
    public R<String> clean(){
        LambdaQueryWrapper<ShoppingCart> qw = new LambdaQueryWrapper<>();
        Long currentId = BaseContext.getCurrentId();
        qw.eq(ShoppingCart::getUserId,currentId);
        shoppingCartService.remove(qw);
        return R.success("清空购物车成功");
    }
}