package com.moon.zksample.mapper;

import com.moon.zksample.domain.po.User;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface UserMapper extends TkMapper<User>{

}
