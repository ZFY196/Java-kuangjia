# MyBatis 与 MyBatis-Plus 图书管理实验

本实验以图书管理为主题，统一连接本机 MySQL 的 `mybatisdb`。保留原有 Maven 目录、`cn.edu.course.mybatis` 包，以及 CRUD、动态 SQL、多表联合查询、条件构造器、分页和七项 MP 扩展功能。

## 一、实验目标与环境

完成图书的新增、查询、修改和逻辑删除；使用动态 SQL 组合书名、装帧、出版社、销售状态和最低定价条件；掌握 association、collection、分页、自动填充与乐观锁。

环境：本机 JDK 17、Maven、MySQL 8；编译目标 Java 8，MyBatis-Plus 3.5.3.1，JUnit 4。连接配置在 `src/main/resources/database.properties`，IDEA 数据源也指向 `mybatisdb`。

本机 JDBC URL 含 `allowPublicKeyRetrieval=true`，用于当前 localhost 环境的 MySQL 公钥认证。运行编码使用 UTF-8。

## 二、数据库设计

五张业务表保留原实验的字段类型、可空规则与关联结构，业务含义统一改为图书。

| 表 | 内容与主要字段 | 初始样例 |
| --- | --- | --- |
| `book` | `book_id`、`book_title`、`binding`、`publisher`、`category`、`price`、`publish_date`、`status`、`publisher_id`、四个 MP 字段 | 10 本 |
| `publisher` | `publisher_id`、`publisher_name`、`city`、`deleted` | 4 家 |
| `book_detail` | `detail_id`、唯一外键 `book_id`、`isbn`、`print_date` | 10 条 |
| `author` | `author_id`、`author_name`、`birth_date` | 6 位 |
| `book_author` | `book_id`、`author_id`，联合主键、两条外键 | 14 条 |

关系：图书与详情是一对一；图书到出版社是多对一，出版社到图书是一对多；图书与作者经 `book_author` 建立多对多。唯一外键限制一本书至多一条详情；LEFT JOIN 保留没有详情或作者的图书。

`binding` 使用“平/精”表示平装/精装；`status=1/0` 表示在售/下架。`deleted=0/1` 表示正常/逻辑删除，两种状态独立，下架图书仍可按编号查询。`create_time`、`update_time`、`version` 分别用于自动填充和乐观锁。

书名、出版社、作者及书号均为教学样例；`LAB-BOOK-xxxxxx` 是实验标识。`publisher` 保存便于单表实验的名称，`publisher_id` 维护真实关联；调整出版社时应一起更新这两个字段。

## 三、准备与运行

本机已完成替换，无需再次建库。新环境在 IDEA 数据库控制台按顺序执行：

1. `sql/book_init.sql`：建图书表并加入中文样例。
2. `sql/book_relations_init.sql`：建出版社、详情、作者、中间表，补充图书外键与关联数据。
3. `sql/book_verification.sql`：观察统计和关联查询结果。

前两份脚本可重复执行，不会重复加入相同样例，也不会重置已有图书。初始结果应为 10 本书、9 本在售、定价合计 681.80 元。`MyBatis从入门到实践` 对应两位作者，可用于观察多对多映射。

在 IDEA 中重新加载 Maven 项目后，选择以下运行配置，或打开对应类运行 `main()`：

| 入口（均在 `cn.edu.course.mybatis.test`） | 演示内容 |
| --- | --- |
| `MyBatisDemo` | 原生 MyBatis 图书目录、按编号查询，并调用多表演示 |
| `MultiTableRelationDemo` | 图书详情、出版社、出版社图书集合、双向图书作者查询 |
| `MyBatisPlusDemo` | 两种 Wrapper、分页、七项 MP 扩展功能 |
| `BookPlusFeaturesDemo` | 单独观察自动填充、乐观锁等七项功能 |

`MultiTableRelationDemo` 参数为 `图书编号 [出版社编号] [作者编号]`；初始样例可用 `1 1 1`，也可不传参数自动选择。

`MyBatisPlusDemo` 参数为 `书名片段 装帧 最低定价 页码 每页数量`，例如 `MyBatis 平 60 1 3`。不传参数时只查在售图书，每页 3 本，初始共 9 条、3 页，按定价降序、编号升序排序。

演示中的临时图书和改动会在结束时回滚；下次运行仍使用原样例。

### IDEA 提示找不到主类时

如果出现 `ClassNotFoundException: cn.edu.course.mybatis.test.MyBatisPlusDemo`，先检查运行配置使用模块 `mybatis-book-practice`，并保留“运行前：构建”。四个演示入口位于 `src/main/java`，应按“应用程序”运行；JUnit 测试位于 `src/test/java`。

执行 IDEA 的“构建 → 重新构建项目”，完成后再运行。仅重新加载 Maven 或普通构建可能继续使用旧的增量编译状态；完整重建会重新生成 `target/classes` 中的主程序和 `target/test-classes` 中的测试类。本机已通过完整重建修复主类缺失问题。

## 四、实验任务

### 任务 1：实体映射与 CRUD

查看 `entity/Book.java`、`mapper/BookMapper.java` 和同名 XML。`BookMapper extends BaseMapper<Book>` 同时提供 XML 方法和 MP 通用方法。

| XML 方法 | 任务 |
| --- | --- |
| `listBooks()` | 查询全部未逻辑删除的图书 |
| `findRecord(key)` | 按编号查询 |
| `createRecord(record)` | 新增并回填自增编号 |
| `replaceDetails(record)` | 更新书名、装帧、出版社、分类、定价、状态，保持出版日期 |
| `removeRecord(key)` | 逻辑删除，保留物理行 |

在 `BookMapperTest` 中观察“MyBatis实战实验”的新增、定价修改与删除。三个测试独立提交，在新会话中检查保存结果，结束后仅清理本测试创建的记录。

MP 对应方法为 `insert`、`selectById`、`updateById`、`deleteById`；XML 和 MP 均排除已逻辑删除的记录。

### 任务 2：动态 SQL

条件对象 `BookSearch` 独立于实体，去除文字两端空格，忽略空条件，拒绝负数最低定价。零定价和下架状态 0 都是有效条件。

| 方法 | MyBatis 标签与行为 |
| --- | --- |
| `searchBooks(criteria)` | `if`、`where` 组合条件，书名包含匹配 |
| `searchBooksWithTrim(criteria)` | 使用 `trim` 得到同等查询结果 |
| `patchDetails(change)` | `set` 只更新提供的字段，支持出版社编号 |
| `addBooks(newcomers)` | `foreach` 批量新增并回填各条编号 |
| `removeBooks(bookKeys)` | `foreach` 批量逻辑删除 |
| `findPreferred(criteria)` | `choose` 优先书名精确匹配，其次出版社，否则全部 |

空/null 批量集合影响行数为 0；局部更新缺编号或没有可修改字段时也返回 0。运行 `BookMapperDynamicSqlTest`，比较 XML、trim 和 Wrapper 对同一条件的查询结果。

### 任务 3：多表联合查询

| 关系 | `MultiTableMapper` 方法 | XML 映射 |
| --- | --- | --- |
| 一对一 | `findBookWithDetail(bookId)` | `association` → `Book.detail` |
| 多对一 | `findBookWithPublisher(bookId)` | `association` → `Book.publisherInfo` |
| 一对多 | `findPublisherWithBooks(publisherId)` | `collection` → `Publisher.books` |
| 多对多 | `findAuthorWithBooks(authorId)` | `collection` → `Author.books` |
| 多对多反向 | `findBookWithAuthors(bookId)` | `collection` → `Book.authors` |

观察 `notNullColumn` 如何避免生成空关联对象，以及 `<id>` 如何将联查的多行合并为一个主对象。`MultiTableMapperTest` 还验证无关联对象、缺失编号、已删除图书过滤，以及通过 XML 新增/修改出版社外键后关联查询同步变化。

### 任务 4：条件构造器与分页

`BookQueryWrappers` 提供 `QueryWrapper` 和 `LambdaQueryWrapper`，复用书名、装帧、出版社、销售状态与最低定价条件。运行 `MyBatisPlusDemo`，比较两种构造器的结果，并观察分页总数与本页数据。

运行 `BookMapperPlusTest`，验证 CRUD、组合筛选、稳定排序、末页、超出末页和空结果。XML SQL 与 Wrapper 更新可以同时使用。

### 任务 5：七项 MyBatis-Plus 扩展功能

| 功能 | 图书实验中的观察点 |
| --- | --- |
| 自动填充 | 新书创建/更新时间非空；更新只刷新更新时间 |
| 逻辑删除 | `@TableLogic` 将 `deleted` 置为 1；下架状态与删除独立 |
| 乐观锁 | 两份旧版本图书快照先后改价，过期版本更新为 0 行 |
| 字段策略 | 分类 `category` 为 null 时保留原值，显式 `set(null)` 可清空 |
| 部分字段 | `selectBriefBooks` 仅查编号、书名、出版社、定价；使用 `selectMaps` 观察列名 |
| 分组聚合 | `summarizePublishers` 按出版社统计图书数量与平均定价 |
| Map 查询 | `selectByMap` 用数据库列名，如 `book_title`、`publisher`、`status` |

运行 `BookPlusFeaturesDemo` 或 `BookMapperAdvancedTest`。过期版本应输出 `Updates: 0`，逻辑删除后 MP/XML 查询都不返回该图书，物理行仍在。

XML 更新同样推进 `version` 并刷新 `update_time`；传入非 null 版本时附加比较。更新时未提供版本的局部对象不执行版本比较。对同一图书读取两份版本快照前需清理会话缓存，避免拿到同一个对象。Wrapper 自动填充需传入非 null 实体。

### 任务 6：事务、日志与构建链路

`MyBatisUtil.openClassicSession()` 使用原生构建器，`openSession()` 使用 MP：解析 XML → 注册乐观锁/分页插件 → 配置自动填充处理器 → 构建 MP 工厂。分页插件放在乐观锁插件后。

会话默认关闭自动提交，只有 `session.commit()` 才永久保存。大多数测试结束后回滚；JDBC 直接写入时使用 `session.rollback(true)` 强制回滚。

日志中 `Preparing` 是参数化 SQL，`Parameters` 是实参，`Total` 是返回行数，`Updates` 是影响行数。先看最后的根异常，再判断是连接、映射还是 SQL 问题。

## 五、验证与实验记录

```bash
mvn clean test
mvn -Dtest=BookMapperTest test
mvn -Dtest=BookMapperDynamicSqlTest test
mvn -Dtest=BookMapperPlusTest,BookMapperAdvancedTest test
mvn -Dtest=MultiTableMapperTest test
```

本机终端若没有配置 Maven PATH，可在 PowerShell 使用 IDEA 自带的 Maven：

```powershell
& 'E:/Program Files/JetBrains/IntelliJ IDEA 2025.3.3/plugins/maven/lib/maven3/bin/mvn.cmd' clean test
```

完整测试共 31 项，包含 CRUD/回滚 5 项、动态 SQL 9 项、MP 基础功能 5 项、扩展功能 7 项、多表关系与外键维护 5 项。提交实验时记录一次 CRUD、一次组合查询、一页分页、五种关系、一次旧版本更新失败和一次回滚后的查询结果。

## 六、本机替换备份

2026-10-15 替换前的 `mybatisdb` 完整 SQL 和员工版项目源码保存在 `.local-backups/20261015-102741-before-books/`，文件分别为 `mybatisdb.sql`、`employee-project.zip`，不纳入版本控制。当前 `mybatisdb` 仅保留五张图书业务表。
