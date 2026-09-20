# MyBatis 实验一前两项任务

本项目完成实验一的任务 1 和任务 2：Maven 项目初始化、MyBatis 配置、`emp` 表 CRUD、Log4j 日志和 JUnit 测试。

## 数据库

表结构保持指导书要求不变，初始化脚本在 `sql/ssm_emp_init.sql`。测试数据已全部替换为新的员工数据。

当前已按 `root/root` 配好 `src/main/resources/database.properties`。运行程序或测试时，如果本机还没有 `ssm_emp` 数据库，会自动执行内置初始化脚本。

## 运行

```bash
mvn test
mvn -DskipTests package
```

演示入口：

```text
cn.edu.course.mybatis.LabOneRunner
```

IDEA 中已添加两个运行配置：

- `LabOneRunner`
- `EmployeeMapperCrudTest`
