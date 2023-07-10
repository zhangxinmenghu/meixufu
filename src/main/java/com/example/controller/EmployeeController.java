package com.example.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.R;
import com.example.entity.Employee;
import com.example.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.apache.tomcat.util.buf.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;


    //员工登录
    @ResponseBody
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){
        String password = employee.getPassword();
        //md5加密密码
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        //根据用户名查数据库
        LambdaQueryWrapper<Employee> qw = new LambdaQueryWrapper<>();
        //查询条件
        qw.eq(Employee::getUsername,employee.getUsername());
        //根据查询条件获取查询到的员工
        Employee emp = (Employee) employeeService.getOne(qw);
        if (emp == null){
            return R.error("登录失败,没有查询到此用户");
        }
        //密码比对
        if (!emp.getPassword().equals(password)){
            return R.error("密码错误，请重新输入！");
        }else if (emp.getStatus() == 0){
            //员工状态为0则是禁用
            return R.error("该员工账号已被禁用！");
        }else {
            //登录成功，将员工id放入Session并返回登录成功结果
            request.getSession().setAttribute("employee",emp.getId());
            return R.success(emp);
        }
    }

    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        //清理session
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }


    //新增员工
    @PostMapping
    public R<String> save(HttpServletRequest request,@RequestBody Employee employee){
        log.info("新增员工，员工信息：{}",employee.toString());
        //设置初始密码123456，md5加密
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());
//        Long empId = (Long) request.getSession().getAttribute("employee");
//        employee.setCreateUser(empId);
//        employee.setUpdateUser(empId);
        employeeService.save(employee);
        return R.success("新增员工成功");
    }

    //分页查询员工信息
    @GetMapping("/page")
    public R<Page>  page(int page,int pageSize,String name){
        log.info("page:{},pagesize:{},name:{}",page,pageSize,name);

        //构造分页构造器
        Page pageInfo = new Page(page,pageSize);
        //构造条件构造器
        LambdaQueryWrapper<Employee> qw = new LambdaQueryWrapper();
        //添加过滤条件
        qw.like(Strings.isNotEmpty(name),Employee::getName,name);
        //添加排序条件
        qw.orderByDesc(Employee::getUpdateTime);
        //执行查询
        employeeService.page(pageInfo,qw);
        return R.success(pageInfo);
    }

    //修改员工信息
    @PutMapping
    public R<String> update(@RequestBody Employee employee, HttpServletRequest request, HttpServletResponse response){
//        employee.setUpdateTime(LocalDateTime.now());
//        Long empId = (Long) request.getSession().getAttribute("employee");
//        employee.setUpdateUser(empId);
        employeeService.updateById(employee);

        return R.success("员工信息修改成功");
    }

    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id){
        log.info("根据id查询员工信息");
        Employee emp = employeeService.getById(id);
        if (emp != null){
            return R.success(emp);
        }
        return R.error("没有查询到对应员工信息");
    }
}
