package cn.edu.course.mybatis.util;

import cn.edu.course.mybatis.mapper.BookMapper;
import cn.edu.course.mybatis.config.MyMetaObjectHandler;
import cn.edu.course.mybatis.config.MybatisPlusConfig;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.MybatisXMLConfigBuilder;
import com.baomidou.mybatisplus.core.MybatisSqlSessionFactoryBuilder;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import java.io.IOException;
import java.io.Reader;
import java.util.function.Function;

public final class MyBatisUtil {
    private static final String SETTINGS = "mybatis-config.xml";

    private MyBatisUtil() {
    }

    private static class ClassicEngine {
        private static final SqlSessionFactory INSTANCE = createFactory(false);
    }

    private static class PlusEngine {
        private static final SqlSessionFactory INSTANCE = createFactory(true);
    }

    private static SqlSessionFactory createFactory(boolean enhanced) {
        try (Reader configuration = Resources.getResourceAsReader(SETTINGS)) {
            if (!enhanced) {
                return new SqlSessionFactoryBuilder().build(configuration);
            }
            // 四步链路：MP 解析 XML → 插件 → GlobalConfig 填充 → MP 工厂。
            MybatisConfiguration parsed = (MybatisConfiguration)
                    new MybatisXMLConfigBuilder(configuration).parse();
            parsed.addInterceptor(MybatisPlusConfig.buildInterceptor());
            GlobalConfig globalConfig = GlobalConfigUtils.getGlobalConfig(parsed);
            globalConfig.setMetaObjectHandler(new MyMetaObjectHandler());
            GlobalConfigUtils.setGlobalConfig(parsed, globalConfig);
            return new MybatisSqlSessionFactoryBuilder().build(parsed);
        } catch (IOException exception) {
            throw new IllegalStateException("读取 MyBatis 实验配置失败：" + SETTINGS, exception);
        }
    }

    public static SqlSession openSession() {
        return PlusEngine.INSTANCE.openSession(false);
    }

    public static SqlSession openClassicSession() {
        return ClassicEngine.INSTANCE.openSession(false);
    }

    public static SqlSessionFactory getSqlSessionFactory() {
        return PlusEngine.INSTANCE;
    }

    public static <T> T inspect(Function<BookMapper, T> work) {
        try (SqlSession session = openSession()) {
            return work.apply(session.getMapper(BookMapper.class));
        }
    }

    public static <T> T inspectClassic(Function<BookMapper, T> work) {
        try (SqlSession session = openClassicSession()) {
            return work.apply(session.getMapper(BookMapper.class));
        }
    }
}
