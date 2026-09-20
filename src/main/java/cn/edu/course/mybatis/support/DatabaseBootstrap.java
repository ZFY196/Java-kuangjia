package cn.edu.course.mybatis.support;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public final class DatabaseBootstrap {
    private static final String CONFIG = "database.properties";
    private static final String SCRIPT = "sql/ssm_emp_init.sql";

    private DatabaseBootstrap() {
    }

    public static void ensureReady() {
        Properties properties = loadProperties();
        String driver = properties.getProperty("jdbc.driver");
        String databaseUrl = properties.getProperty("jdbc.url");
        String username = properties.getProperty("jdbc.username");
        String password = properties.getProperty("jdbc.password");

        try {
            Class.forName(driver);
            if (canReadEmployeeTable(databaseUrl, username, password)) {
                return;
            }
            runInitScript(toServerUrl(databaseUrl), username, password);
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException("未找到 MySQL JDBC 驱动", ex);
        }
    }

    private static boolean canReadEmployeeTable(String url, String username, String password) {
        try (Connection connection = DriverManager.getConnection(url, username, password);
             Statement statement = connection.createStatement()) {
            statement.executeQuery("SELECT 1 FROM emp LIMIT 1");
            return true;
        } catch (SQLException ex) {
            return false;
        }
    }

    private static void runInitScript(String url, String username, String password) {
        try (Connection connection = DriverManager.getConnection(url, username, password);
             Statement statement = connection.createStatement()) {
            for (String sql : loadSqlScript().split(";")) {
                String command = sql.trim();
                if (!command.isEmpty()) {
                    statement.execute(command);
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("初始化 ssm_emp 数据库失败", ex);
        }
    }

    private static String toServerUrl(String databaseUrl) {
        int queryIndex = databaseUrl.indexOf('?');
        String query = queryIndex >= 0 ? databaseUrl.substring(queryIndex) : "";
        String main = queryIndex >= 0 ? databaseUrl.substring(0, queryIndex) : databaseUrl;
        int slashIndex = main.lastIndexOf('/');
        return main.substring(0, slashIndex + 1) + query;
    }

    private static Properties loadProperties() {
        try (InputStream input = DatabaseBootstrap.class.getClassLoader().getResourceAsStream(CONFIG)) {
            if (input == null) {
                throw new IllegalStateException("未找到 " + CONFIG);
            }
            Properties properties = new Properties();
            properties.load(input);
            return properties;
        } catch (IOException ex) {
            throw new IllegalStateException("读取数据库配置失败", ex);
        }
    }

    private static String loadSqlScript() {
        try (InputStream input = DatabaseBootstrap.class.getClassLoader().getResourceAsStream(SCRIPT)) {
            if (input == null) {
                throw new IllegalStateException("未找到 " + SCRIPT);
            }
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int length;
            while ((length = input.read(buffer)) != -1) {
                output.write(buffer, 0, length);
            }
            return new String(output.toByteArray(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("读取数据库初始化脚本失败", ex);
        }
    }
}
