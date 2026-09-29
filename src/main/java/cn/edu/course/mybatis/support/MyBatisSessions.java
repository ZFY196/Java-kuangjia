package cn.edu.course.mybatis.support;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import com.baomidou.mybatisplus.core.MybatisSqlSessionFactoryBuilder;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;

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
            SqlSessionFactory factory = new MybatisSqlSessionFactoryBuilder().build(input);
            MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
            interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
            factory.getConfiguration().addInterceptor(interceptor);
            return factory;
        } catch (IOException ex) {
            throw new IllegalStateException("无法读取 MyBatis 配置文件", ex);
        }
    }
}
