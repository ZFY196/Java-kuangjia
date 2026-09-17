package com.example;

import com.example.entity.User;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 注解方式 Mapper 测试类
 */
public class UserMapperAnnotationTest {

    private InputStream is;
    private SqlSession session;
    private UserMapperAnnotation userMapper;

    @Before
    public void init() throws IOException {
        is = Resources.getResourceAsStream("mybatis-config.xml");
        SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(is);
        session = factory.openSession(true);
        userMapper = session.getMapper(UserMapperAnnotation.class);

        // 确保演示数据存在（幂等）：插入"张三"，若用户名已存在则自动跳过
        // 供第2、3学时新增测试使用（login / findUserByMap / findByUsername / SQL注入演示）
        try {
            java.sql.Statement st = session.getConnection().createStatement();
            st.execute("INSERT IGNORE INTO user(username, password, email, create_time) "
                    + "VALUES ('张三', '123456', 'zhangsan@qq.com', NOW())");
            st.close();
        } catch (Exception e) {
            System.out.println("跳过演示数据插入：" + e.getMessage());
        }
    }

    @After
    public void destroy() throws IOException {
        if (session != null) session.close();
        if (is != null) is.close();
    }

    @Test
    public void testFindAll() {
        List<User> users = userMapper.findAll();
        for (User user : users) {
            System.out.println(user);
        }
        assert users.size() > 0;
    }

    @Test
    public void testFindByNameAndEmail() {
        List<User> users = userMapper.findByNameAndEmail("张三", "zhangsan@qq.com");
        for (User user : users) {
            System.out.println(user);
        }
    }

    @Test
    public void testFindByNameLike() {
        // 注意：这里存在 SQL 注入风险（演示用）
        List<User> users = userMapper.findByNameLike("张");
        for (User user : users) {
            System.out.println(user);
        }
    }

    // ==================== 第2学时：@Param 多参数传递 ====================

    @Test
    public void testLogin() {
        // 正确使用 @Param 注解传递多个参数
        User user = userMapper.login("张三", "123456");
        System.out.println("登录结果：" + user);
        assert user != null : "使用 @Param 后多参数查询应成功";
    }

    @Test
    public void testFindUserByMap() {
        // 参数较多时使用 Map 集合传递
        Map<String, Object> map = new HashMap<>();
        map.put("username", "张三");
        map.put("email", "zhangsan@qq.com");
        map.put("password", "123456");

        List<User> users = userMapper.findUserByMap(map);
        for (User user : users) {
            System.out.println(user);
        }
        assert users.size() > 0 : "Map 传参查询应有结果";
    }

    // ==================== 第3学时：#{} 和 ${} 对比实验 ====================

    @Test
    public void testFindByUsername() {
        // #{}：观察日志 ==> Preparing: SELECT * FROM user WHERE username = ?
        //              ==> Parameters: 张三(String)
        User user = userMapper.findByUsername("张三");
        System.out.println("查询结果：" + user);
        assert user != null;
    }

    @Test
    public void testFindByUsername2() {
        // ${}：观察日志 ==> Preparing: SELECT * FROM user WHERE username = '张三'
        //              ==> Parameters:（无参数输出，直接拼接到 SQL）
        User user = userMapper.findByUsername2("张三");
        System.out.println("查询结果：" + user);
        assert user != null;
    }

    @Test
    public void testSqlInjection() {
        // 正常输入
        User user1 = userMapper.loginUnsafe("张三", "123456");
        System.out.println("正常登录：" + user1);
        assert user1 != null;

        // SQL 注入攻击：username 传入 "张三' -- "，密码条件被注释掉，绕过密码校验直接登录
        // 实际执行的 SQL：SELECT * FROM user WHERE username = '张三' -- ' AND password = '任意密码'
        User user2 = userMapper.loginUnsafe("张三' -- ", "任意密码");
        System.out.println("SQL注入登录：" + user2);
        // 结果：user2 不为 null，登录成功（攻击成功！）
        assert user2 != null : "${} 拼接存在 SQL 注入漏洞";
    }

    @Test
    public void testSqlInjectionSafe() {
        // 同样的注入字符串，使用 #{} 预编译后攻击失败
        User user = userMapper.loginSafe("' OR '1'='1' -- ", "任意密码");
        System.out.println("安全登录：" + user);
        // 结果：user 为 null，登录失败（攻击失败！）
        assert user == null : "#{} 预编译可防御 SQL 注入";
    }
}
