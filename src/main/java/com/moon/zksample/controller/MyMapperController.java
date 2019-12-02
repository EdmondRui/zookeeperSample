package com.moon.zksample.controller;


import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.moon.zksample.domain.po.User;
import com.moon.zksample.mapper.UserMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/mapper")
public class MyMapperController {

    @Resource
    UserMapper userMapper;

    @GetMapping("/get1")
    public Object getUser1() {
       return userMapper.selectByPrimaryKey(1);
    }

    @GetMapping("/get2")
    public Object getUser2() {
        Example example = new Example(User.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("name", "张三");
        User user = new User();
        user.setName("张三");
        return userMapper.select(user);
//        return userMapper.selectByExample(example);
    }

    @GetMapping("/page")
    public Object getPage() {
        PageHelper.startPage(1, 10);
        List<User> list = userMapper.selectAll();
        PageInfo<User> page = new PageInfo<>(list);
        return page;
    }

}
