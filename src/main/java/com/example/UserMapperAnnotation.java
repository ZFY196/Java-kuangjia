package com.example;

import com.example.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

/**
 * 用户 Mapper 接口（注解方式）
 */
public interface UserMapperAnnotation {

    /**
     * 查询所有用户（@Select 注解）
     */
    @Select("SELECT * FROM user")
    List<User> findAll();

    /**
     * 根据 ID 查询用户
     */
    @Select("SELECT * FROM user WHERE id = #{id}")
    User findById(Integer id);

    /**
     * 添加用户（@Insert 注解 + 获取自增主键）
     */
    @Insert("INSERT INTO user(username, password, email) VALUES(#{username}, #{password}, #{email})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int addUser(User user);

    /**
     * 更新用户（@Update 注解）
     */
    @Update("UPDATE user SET username=#{username}, email=#{email} WHERE id=#{id}")
    int updateUser(User user);

    /**
     * 删除用户（@Delete 注解）
     */
    @Delete("DELETE FROM user WHERE id = #{id}")
    int deleteUser(Integer id);

    /**
     * 多条件查询（@Param 注解）
     */
    @Select("SELECT * FROM user WHERE username=#{name} AND email=#{email}")
    List<User> findByNameAndEmail(@Param("name") String username, @Param("email") String email);

    /**
     * 模糊查询（${} 用法，注意 SQL 注入风险）
     */
    @Select("SELECT * FROM user WHERE username LIKE '%${username}%'")
    List<User> findByNameLike(@Param("username") String username);

    /**
     * 动态表名查询（${} 正确用法）
     */
    @Select("SELECT * FROM ${tableName}")
    List<User> findAllByTableName(@Param("tableName") String tableName);

    // ==================== 第2学时：@Param 多参数传递 ====================

    /**
     * 3.1 【错误示例】多个参数不使用 @Param，运行时会报错：
     * Parameter 'username' not found. Available parameters are [arg0, arg1, param1, param2]
     * 如需复现，取消下面两行注释并调用（演示用，默认保留为注释）
     */
    // @Select("SELECT * FROM user WHERE username=#{username} AND password=#{password}")
    // User login(String username, String password);

    /**
     * 3.2 【正确示例】使用 @Param 指定参数名，解决多参数传递问题
     */
    @Select("SELECT * FROM user WHERE username=#{username} AND password=#{password}")
    User login(@Param("username") String username, @Param("password") String password);

    /**
     * 3.3 【Map 传参】参数较多时使用 Map 集合，key 对应 SQL 中 #{} 的名称
     */
    @Select("SELECT * FROM user WHERE username=#{username} AND email=#{email} AND password=#{password}")
    List<User> findUserByMap(Map<String, Object> map);

    // ==================== 第3学时：#{} 和 ${} 对比实验 ====================

    /**
     * 4.1 #{} 占位符（安全）：PreparedStatement，参数通过 ? 传入
     * 日志：==>  Preparing: SELECT * FROM user WHERE username = ?
     *       ==> Parameters: 张三(String)
     */
    @Select("SELECT * FROM user WHERE username = #{username}")
    User findByUsername(@Param("username") String username);

    /**
     * 4.2 ${} 字符串拼接（不安全）：参数直接拼接到 SQL 中
     * 日志：==>  Preparing: SELECT * FROM user WHERE username = '张三'
     *       ==> Parameters:（无参数输出）
     */
    @Select("SELECT * FROM user WHERE username = '${username}'")
    User findByUsername2(@Param("username") String username);

    /**
     * 4.3 SQL 注入演示（危险写法，${} 直接拼接）
     */
    @Select("SELECT * FROM user WHERE username = '${username}' AND password = '${password}'")
    User loginUnsafe(@Param("username") String username, @Param("password") String password);

    /**
     * 4.3 SQL 注入防御（安全写法，#{} 预编译）
     */
    @Select("SELECT * FROM user WHERE username = #{username} AND password = #{password}")
    User loginSafe(@Param("username") String username, @Param("password") String password);
}