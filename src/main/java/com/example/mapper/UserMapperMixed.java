package com.example.mapper;

import com.example.entity.User;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户 Mapper 接口（XML 与注解混合开发）
 * 简单查询用注解（@Select），复杂查询用 XML（UserMapperMixed.xml 中配置动态 SQL）
 */
public interface UserMapperMixed {

    /**
     * 简单查询：使用注解
     */
    @Select("SELECT * FROM user WHERE id = #{id}")
    User findById(Integer id);

    /**
     * 复杂查询：使用 XML（动态 SQL，见 UserMapperMixed.xml）
     */
    List<User> findByCondition(User user);
}
