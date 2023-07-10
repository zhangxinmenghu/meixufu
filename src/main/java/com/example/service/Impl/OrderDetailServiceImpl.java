package com.example.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.example.entity.OrderDetail;
import com.example.mapper.OrderDetailMapper;
import com.example.service.OrderDetailService;
import org.springframework.stereotype.Service;

@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail> implements OrderDetailService {

}