# 实验一任务 1、任务 2 完成说明

## 任务 1：开发环境搭建与项目初始化

已创建 Maven 工程：

- `pom.xml`：引入 MyBatis、MySQL 驱动、Log4j、JUnit。
- `sql/ssm_emp_init.sql`：创建 `ssm_emp` 数据库和 `emp` 表。
- `src/main/resources/database.properties`：集中维护数据库连接参数。

数据库结构与指导书一致，测试数据已全部替换。

Maven 镜像可在本机 `settings.xml` 中配置：

```xml
<mirror>
    <id>aliyun-public</id>
    <mirrorOf>*</mirrorOf>
    <name>aliyun maven mirror</name>
    <url>https://maven.aliyun.com/repository/public</url>
</mirror>
```

Git 初始化后建议提交一次：

```bash
git add .
git commit -m "init: complete mybatis lab task 1 and task 2"
```

AI 插件可任选通义灵码、GitHub Copilot 或 CodeGeeX。实验报告中建议记录：辅助生成依赖清单、检查 MyBatis 配置、解释 SQL 日志、排查数据库连接问题。

## 任务 2：MyBatis 环境搭建与基础 CRUD

主要文件：

- `src/main/resources/mybatis-config.xml`：MyBatis 全局配置。
- `src/main/resources/log4j.properties`：控制台 SQL 日志。
- `src/main/java/cn/edu/course/mybatis/domain/Employee.java`：员工实体。
- `src/main/java/cn/edu/course/mybatis/mapper/EmployeeMapper.java`：Mapper 接口。
- `src/main/resources/cn/edu/course/mybatis/mapper/EmployeeMapper.xml`：SQL 映射。
- `src/main/java/cn/edu/course/mybatis/support/MyBatisSessions.java`：SqlSessionFactory 工具类。
- `src/main/java/cn/edu/course/mybatis/LabOneRunner.java`：查询演示入口。
- `src/test/java/cn/edu/course/mybatis/EmployeeMapperCrudTest.java`：增删改查单元测试。

降低重复度处理：

- 包名、类名、方法名均未照搬指导书示例。
- 实体属性改为业务语义命名，通过 `resultMap` 显式映射数据库字段。
- 测试用例不依赖固定主键，新增数据后在同一事务中验证并回滚。
- 数据库账号密码从 MyBatis 主配置中拆到 `database.properties`。
