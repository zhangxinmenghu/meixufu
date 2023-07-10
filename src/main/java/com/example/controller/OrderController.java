package com.example.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.BaseContext;
import com.example.common.R;
import com.example.dto.OrderDto;
import com.example.entity.OrderDetail;
import com.example.entity.Orders;
import com.example.service.OrderDetailService;
import com.example.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单
 */
@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderDetailService orderDetailService;

    /**
     * 用户下单
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        log.info("订单数据：{}",orders);
        orderService.submit(orders);
        return R.success("下单成功");
    }

    @GetMapping("/userPage")
    public R<Page> page(int page,int pageSize){
        Page<Orders> pageInfo = new Page<>(page,pageSize);
        Page<OrderDto> pageDto = new Page<>(page,pageSize);
        Long currentId = BaseContext.getCurrentId();

        LambdaQueryWrapper<Orders> qw = new LambdaQueryWrapper<>();
        qw.eq(Orders::getUserId,currentId);

        qw.orderByDesc(Orders::getOrderTime);
        orderService.page(pageInfo,qw);

        //根据orderId查询对应的OrderDetails
        LambdaQueryWrapper<OrderDetail> orderQw = new LambdaQueryWrapper<>();

        //对orderDto需要的属性赋值
        List<Orders> records = pageInfo.getRecords();

        List<OrderDto> orderDtoList = records.stream().map((item) -> {
            OrderDto orderDto = new OrderDto();
            //此时orderDto下的orderDetails还为空，进行赋值

            //获取订单ID
            Long orderId = item.getId();
            List<OrderDetail> orderDetailList = this.getOrderDetailListByOrderId(orderId);
            BeanUtils.copyProperties(item,orderDto);
            //对orderDto进行OrderDetails属性的赋值
            orderDto.setOrderDetails(orderDetailList);
            return orderDto;
        }).collect(Collectors.toList());
        BeanUtils.copyProperties(pageInfo,pageDto,"records");
        pageDto.setRecords(orderDtoList);
        return R.success(pageDto);

    }

    //抽离的一个方法，通过订单id查询订单明细，得到一个订单明细的集合
    //这里抽离出来是为了避免在stream中遍历的时候直接使用构造条件来查询导致eq叠加，从而导致后面查询的数据都是null
    private List<OrderDetail> getOrderDetailListByOrderId(Long orderId) {
        LambdaQueryWrapper<OrderDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderDetail::getOrderId, orderId);
        List<OrderDetail> orderDetailList = orderDetailService.list(queryWrapper);
        return orderDetailList;
    }


    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String number, String beginTime, String endTime){
        //分页构造器对象
        Page<Orders> pageInfo = new Page<>(page,pageSize);
        //构造条件查询对象
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();

        //添加查询条件  动态sql  字符串使用StringUtils.isNotEmpty这个方法来判断
        //这里使用了范围查询的动态SQL，这里是重点！！！
        queryWrapper.like(number!=null,Orders::getNumber,number)
                .gt(Strings.isNotEmpty(beginTime),Orders::getOrderTime,beginTime)
                .lt(Strings.isNotEmpty(endTime),Orders::getOrderTime,endTime);
        orderService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);
    }

    //派送
    @PutMapping
    public R<Orders> dispatch(@RequestBody Orders orders){
        LambdaQueryWrapper<Orders> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(orders.getId()!=null,Orders::getId,orders.getId());
        Orders one = orderService.getOne(queryWrapper);

        one.setStatus(orders.getStatus());
        orderService.updateById(one);
        return R.success(one);
    }
}