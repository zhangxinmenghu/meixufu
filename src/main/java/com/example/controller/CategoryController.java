package com.example.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.R;
import com.example.entity.Category;
import com.example.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;


    //新增菜品
    @PostMapping
    public R<String> save(@RequestBody Category category){
        categoryService.save(category);
        return R.success("新增菜品成功");
    }

    //分页查询
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        Page categoryPage = new Page(page, pageSize);
        LambdaQueryWrapper<Category> categoryQw = new LambdaQueryWrapper();
        categoryQw.like(Strings.isNotEmpty(name),Category::getName,name);
        categoryQw.orderByAsc(Category::getSort);
        categoryService.page(categoryPage,categoryQw);
        return R.success(categoryPage);
    }

    //删除分类
    @DeleteMapping
    public R<String> delete(Long id){
        categoryService.remove(id);
        return R.success("分类信息删除成功");
    }


    @PutMapping
    public R<String> update(@RequestBody Category category){
        categoryService.updateById(category);
        return R.success("更新分类信息成功");

    }


    //根据条件查询分类数据
    @GetMapping("/list")
    public R<List<Category>> list(Category category){
        //条件构造器
        LambdaQueryWrapper<Category> qw = new LambdaQueryWrapper<>();
        //添加条件
        qw.eq(category.getType() != null,Category::getType,category.getType());
        //添加排序条件
        qw.orderByAsc(Category::getType).orderByDesc(Category::getUpdateTime);
        List<Category> list = categoryService.list(qw);
        return R.success(list);
    }
}
