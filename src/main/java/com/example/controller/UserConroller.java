package com.example.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.common.R;
import com.example.entity.User;
import com.example.service.UserService;
import com.example.utils.SMSUtils;
import com.example.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Mapper;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Enumeration;
import java.util.Map;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserConroller {
    @Autowired
    private UserService userService;

    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session){
        //获取手机号
        String phone = user.getPhone();
        //生成随机四位验证码
        if (Strings.isNotEmpty(phone)){
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("电话：{}，验证码：{}",phone,code);
            //调用阿里云api发送短信
            SMSUtils.sendMessage("阿里云短信测试","SMS_154950909",phone,code);
            //需要将生成的验证码保存到session
            session.setAttribute("phone",code);
//
//该部分用于打印session里的值
//
//// 获取session中所有的键值
//            Enumeration<?> enumeration = session.getAttributeNames();
//// 遍历enumeration中的
//            while (enumeration.hasMoreElements()) {
//// 获取session键值
//                String name = enumeration.nextElement().toString();
//                // 根据键值取session中的值
//                Object value = session.getAttribute(name);
//                // 打印结果
//                System.out.println("<B>" + name + "</B>=" + value + "<br>/n");
//            }
            return R.success("手机验证码短信发送成功!");
        }
        return R.error("手机验证码短信发送失败!");
    }

    @PostMapping("/login")
    public R<User> login(@RequestBody Map userMap,HttpSession session){
        log.info(userMap.toString());
        JSONObject json = new JSONObject(userMap);
        log.info(json.toString());
        //获取手机号
        //String phone = userMap.get("phone").toString();
        String phone = (String) json.getJSONObject("data").get("phone");
        log.info(phone);
        //获取验证码   用户提交的验证码
        //String code = userMap.get("code").toString();
        String code = (String) json.getJSONObject("data").get("code");
        log.info(code);
        log.info(session.toString());
        for (Enumeration e = session.getAttributeNames(); e.hasMoreElements(); )
        {
            System.out.println(e.nextElement());
        }
        //获取session里的验证码  短信发送的验证码
        String sessionCode = session.getAttribute("phone").toString();

        //验证码对比
        if ( !(phone != null && code.equals(sessionCode)) ){
            return R.error("登录失败,用户名或密码不正确!");
        }else {
            //登录成功
            LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<>();
            //添加条件查询数据库字段和当前字段是否相同
            qw.eq(User::getPhone,phone);
            //通过getOne获取唯一对象便于以后的注册存储
            User user = userService.getOne(qw);
            //判断当前是否为新用户
            if (user == null){
                //注册
                User userNew = new User();
                userNew.setPhone(phone);
                userNew.setStatus(1);
                userService.save(userNew);
                session.setAttribute("user",userNew.getId());
                return R.success(userNew);
            }
            else {
                //不是新用户
                session.setAttribute("user",user.getId());
                return R.success(user);
            }

        }
    }
}
