package com.example.mapper;

import com.example.entity.User;
import java.util.List;

/**
 * 用户 Mapper 接口
 * 对应 Mapper XML：UserMapper.xml
 */
public interface UserMapper {

    /**
     * 查询所有用户
     * @return 用户列表
     */
    List<User> findAll();

    /**
     * 根据 ID 查询用户
     * @param id 用户ID
     * @return 用户对象
     */
    User findById(Integer id);

    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户对象
     */
    User testfind(String username);

    /**
     * 添加用户
     * @param user 用户对象
     * @return 影响行数
     */
    int addUser(User user);

    /**
     * 更新用户
     * @param user 用户对象
     * @return 影响行数
     */
    int updateUser(User user);

    /**
     * 删除用户
     * @param id 用户ID
     * @return 影响行数
     */
    int deleteUser(Integer id);

    /**
     * 根据用户名模糊查询
     * @param username 用户名关键字
     * @return 用户列表
     */
    List<User> findByUsernameLike(String username);
}
