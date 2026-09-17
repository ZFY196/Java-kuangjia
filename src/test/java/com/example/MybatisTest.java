package com.example;

import com.example.entity.User;
import com.example.mapper.UserMapper;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

/**
 * MyBatis 测试类
 *
 * mybatis中的核心类：
 * SqlSessionFactory：数据库连接工厂
 * SqlSession：数据库连接session，类似connection对象
 */
public class MybatisTest {

    private SqlSessionFactory sqlSessionFactory;  // 定义一个Mybatis的连接工厂

    @Before  // 在测试方法执行前执行
    public void init() throws Exception {
        InputStream is = Resources.getResourceAsStream("mybatis-config.xml");
        // 读取当前项目的数据库连接配置文件mybatis-config.xml
        // 该文件中包含数据库的连接配置和Mapper XML文件的注册
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(is);
        // 创建一个Mybatis的连接工厂

        // 重置数据：清空 user 表并恢复初始 4 条数据，保证每个测试方法都从干净状态开始（顺序无关、可重复运行）
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            java.sql.Statement st = session.getConnection().createStatement();
            st.execute("DELETE FROM user");
            st.execute("ALTER TABLE user AUTO_INCREMENT = 1");
            st.execute("INSERT INTO user(username, password, email, create_time) VALUES "
                    + "('zhangsan', '123456', 'zhangsan@example.com', NOW()),"
                    + "('lisi', '123456', 'lisi@example.com', NOW()),"
                    + "('wangwu', '123456', 'wangwu@example.com', NOW()),"
                    + "('zhaoliu', '123456', 'zhaoliu@example.com', NOW())");
            st.close();
        }
    }

    @Test
    public void testFindAll() {
        System.out.println("========== 测试查询所有用户 ==========");
        SqlSession sqlSession = sqlSessionFactory.openSession();

        // 方式一：通过Mapper代理接口调用
        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
        List<User> users = userMapper.findAll();

        // 方式二：直接读取Mapper XML中的sql方法（注释方式）
        // List<User> users = sqlSession.selectList("com.example.mapper.UserMapper.findAll");

        for (User user : users) {
            System.out.println(user);
        }
        sqlSession.close();
    }

    @Test
    public void testFindByName() {
        System.out.println("========== 测试根据用户名查询 ==========");
        SqlSession sqlSession = sqlSessionFactory.openSession();
        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
        User user = userMapper.testfind("zhangsan");
        System.out.println(user);
        sqlSession.close();
    }

    @Test
    public void testFindById() {
        System.out.println("========== 测试根据ID查询用户 ==========");
        SqlSession sqlSession = sqlSessionFactory.openSession();
        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
        User user = userMapper.findById(1);
        System.out.println(user);
        sqlSession.close();
    }

    @Test
    public void testAddUser() {
        System.out.println("========== 测试添加用户 ==========");
        SqlSession sqlSession = sqlSessionFactory.openSession();
        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
        // 幂等处理：若已存在同名用户，先删除，避免 username 唯一键冲突导致重复运行失败
        User existing = userMapper.testfind("测试用户");
        if (existing != null) {
            userMapper.deleteUser(existing.getId());
            System.out.println("已清理上次遗留的同名用户，id=" + existing.getId());
        }
        User user = new User();
        user.setUsername("测试用户");
        user.setPassword("123456");
        user.setEmail("test@qq.com");
        user.setCreateTime(new Date());
        int rows = userMapper.addUser(user);
        System.out.println("影响行数：" + rows);
        System.out.println("自增主键：" + user.getId());
        sqlSession.commit();
        sqlSession.close();
    }

    @Test
    public void testUpdateUser() {
        System.out.println("========== 测试更新用户 ==========");
        SqlSession sqlSession = sqlSessionFactory.openSession();
        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
        User user = userMapper.findById(1);
        if (user == null) {
            // 兜底：id=1 可能已被删除测试清掉，先新建一条再更新，保证测试与执行顺序无关
            user = new User();
            user.setUsername("待更新用户");
            user.setPassword("123456");
            user.setEmail("temp@qq.com");
            user.setCreateTime(new Date());
            userMapper.addUser(user);
            System.out.println("id=1 不存在，已新建待更新用户，id=" + user.getId());
        }
        user.setUsername("更新后的用户名");
        user.setEmail("update@qq.com");
        user.setUpdateTime(new Date());
        int rows = userMapper.updateUser(user);
        System.out.println("影响行数：" + rows);
        sqlSession.commit();
        sqlSession.close();
    }

    @Test
    public void testDeleteUser() {
        System.out.println("========== 测试删除用户 ==========");
        SqlSession sqlSession = sqlSessionFactory.openSession();
        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
        int rows = userMapper.deleteUser(1);
        System.out.println("影响行数：" + rows);
        sqlSession.commit();
        sqlSession.close();
    }

    @Test
    public void testFindByUsernameLike() {
        System.out.println("========== 测试根据用户名模糊查询 ==========");
        SqlSession sqlSession = sqlSessionFactory.openSession();
        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
        List<User> users = userMapper.findByUsernameLike("张");
        for (User user : users) {
            System.out.println(user);
        }
        sqlSession.close();
    }
}
