package com.example;

import com.example.entity.User;
import com.example.mapper.UserMapperMixed;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * XML 与注解混合开发测试类
 */
public class UserMapperMixedTest {

    private InputStream is;
    private SqlSession session;
    private UserMapperMixed userMapper;

    @Before
    public void init() throws IOException {
        is = Resources.getResourceAsStream("mybatis-config.xml");
        SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(is);
        session = factory.openSession(true);
        userMapper = session.getMapper(UserMapperMixed.class);

        // 确保演示数据存在（幂等）：插入"张三"，若用户名已存在则自动跳过
        // 供 findByCondition 的模糊查询条件（username LIKE '%张%'）使用
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
    public void testFindById() {
        // 注解方式（@Select）查询
        User user = userMapper.findById(1);
        System.out.println(user);
        assert user != null : "注解查询应返回用户";
    }

    @Test
    public void testFindByCondition() {
        // XML 方式（动态 SQL）查询：只传 username 条件，动态拼接 LIKE
        User cond = new User();
        cond.setUsername("张");
        List<User> users = userMapper.findByCondition(cond);
        for (User user : users) {
            System.out.println(user);
        }
        assert users.size() > 0 : "XML 动态条件查询应有结果";
    }
}
