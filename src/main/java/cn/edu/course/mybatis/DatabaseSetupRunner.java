package cn.edu.course.mybatis;

import cn.edu.course.mybatis.support.DatabaseBootstrap;

public class DatabaseSetupRunner {
    public static void main(String[] args) {
        DatabaseBootstrap.ensureReady();
        System.out.println("ssm_emp 数据库已就绪");
    }
}
