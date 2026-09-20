package cn.edu.course.mybatis.support;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;

public final class MyBatisSessions {
    private static final SqlSessionFactory FACTORY = createFactory();

    private MyBatisSessions() {
    }

    public static SqlSession open() {
        return FACTORY.openSession();
    }

    public static SqlSession openAutoCommit() {
        return FACTORY.openSession(true);
    }

    private static SqlSessionFactory createFactory() {
        try (InputStream input = Resources.getResourceAsStream("mybatis-config.xml")) {
            return new SqlSessionFactoryBuilder().build(input);
        } catch (IOException ex) {
            throw new IllegalStateException("无法读取 MyBatis 配置文件", ex);
        }
    }
}
