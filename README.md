# MyBatis 员工实验与 MyBatis-Plus 综合实战

项目沿用 `cn.edu.course.mybatis` 包、`Emp` 员工实体、`EmpMapper` 接口及 `ssm_emp.emp` 表，包含员工 CRUD、动态 SQL、MP 条件与分页、多表联合查询，以及七项 MP 补充功能。

## 数据库准备

连接参数在 `src/main/resources/database.properties`。按顺序执行：

1. `sql/ssm_emp_init.sql`：创建员工表，或为现有表补齐扩展字段，并补充缺少的中文样例。
2. `sql/ssm_multi_table_init.sql`：创建部门、身份证、项目、中间表，补充员工关联数据。

两份脚本均可重复执行，保留原有员工。员工原有字段为 `emp_id`、`emp_name`、`gender`、`dept`、`post`、`salary`、`hire_date`、`status`。综合实战新增 `create_time`、`update_time`、`deleted`、`version`；`status=1/0` 表示在职/离职，`deleted=0/1` 表示正常/逻辑删除。离职员工仍可按编号查询。

`mybatisdb` 中使用 `empno`、`ename`、`deptno` 的另一套表结构与当前实验不同，其中文化脚本是 `sql/mybatisdb_chinese_data.sql`。

## 运行入口

| 入口 | 输出内容 |
| --- | --- |
| `cn.edu.course.mybatis.test.MyBatisDemo` | 原生 MyBatis 员工名册、按编号查询及多表演示 |
| `cn.edu.course.mybatis.test.MultiTableRelationDemo` | 一对一、多对一、一对多、双向多对多 |
| `cn.edu.course.mybatis.test.MyBatisPlusDemo` | QueryWrapper、LambdaQueryWrapper、分页及七项补充功能 |
| `cn.edu.course.mybatis.test.EmpPlusFeaturesDemo` | 单独观察七项补充功能，结束时回滚临时记录 |

IDEA 打开对应类，点击 `main()` 左侧绿色三角运行。完整测试使用 `mvn test`。`.mvn/jvm.config` 和 Surefire 均指定 UTF-8；IDEA 运行时可添加 VM 参数 `-Dfile.encoding=UTF-8`。

`MyBatisPlusDemo` 的可选参数依次为姓名片段、性别、最低薪资、页码、每页数量，例如 `明 男 12000 1 3`。默认查询在职员工，第 1 页每页 3 条。七项功能演示使用单独事务创建临时员工，显示真实 SQL 后回滚；部门统计和简表中可能暂时包含这条演示记录。

## 原生 MyBatis：CRUD 与动态 SQL

`EmpMapper.xml` 显式映射全部员工字段，`EmpMapper` 同时保留 XML 方法与 `BaseMapper<Emp>` 方法。

| XML 方法 | 用途 |
| --- | --- |
| `listRoster()` | 查询全部正常员工 |
| `findRecord(key)` | 按编号查询正常员工 |
| `createRecord(record)` | 新增员工并回填 `id` |
| `replaceProfile(record)` | 更新姓名、性别、部门、岗位、薪资和状态，保持入职日期 |
| `removeRecord(key)` | 按编号逻辑删除 |
| `searchRoster(criteria)` | `if` / `where`：姓名包含匹配，性别、部门、状态及最低薪资组合筛选 |
| `searchRosterWithTrim(criteria)` | `trim` 实现相同筛选 |
| `patchProfile(change)` | `set` 仅更新提供的姓名、部门、岗位、薪资和状态 |
| `addRoster(newcomers)` | `foreach` 批量新增，回填每名员工 `id` |
| `removeRoster(employeeKeys)` | `foreach` 批量逻辑删除 |
| `findPreferred(criteria)` | `choose` 优先姓名精确匹配，否则部门，否则全部正常员工 |

条件对象是 `StaffSearch`，会规范化字符串、忽略空条件，并拒绝负数最低薪资。状态 `0` 和薪资 `0` 是有效值。空/null 批量集合影响行数为 `0`；动态更新缺编号或缺少可修改字段时同样返回 `0`。

XML 查询都显式限制 `deleted=0`，写操作不修改已删除员工。XML 更新推进 `version` 并刷新 `update_time`；传入非 null 的 `version` 时附加版本校验，更新后再次查询可获取最新版本。XML 新增使用数据库时间默认值。写操作只有调用 `session.commit()` 才保存。

`EmpMapperTest` 包含三个独立提交测试，使用“沈知秋 / 数据平台部 / 自动化测试工程师”，验证新增、薪资修改及删除后查询。结束后通过 JDBC 物理清理本测试创建的记录。业务接口仍执行逻辑删除。

```bash
mvn -Dtest=EmpMapperTest test
mvn -Dtest=EmpMapperDynamicSqlTest test
```

`EmpMapperDynamicSqlTest` 的 9 个测试覆盖条件、where/trim 等效性、局部/空更新、批量/空集合、条件规范化、负数校验与 choose 优先级，结束时回滚。

## 多表联合查询

| 关系 | Mapper 方法 | 映射 |
| --- | --- | --- |
| 一对一：员工 → 身份证 | `findEmpWithIdCard(empId)` | `association` → `Emp.idCard` |
| 多对一：员工 → 部门 | `findEmpWithDept(empId)` | `association` → `Emp.deptInfo` |
| 一对多：部门 → 员工 | `findDeptWithEmps(deptno)` | `collection` → `Dept.emps` |
| 多对多：项目 → 员工 | `findProjectWithEmps(projectId)` | `project` / `emp_project` / `emp` 联查 |
| 多对多：员工 → 项目 | `findEmpWithProjects(empId)` | 相同三张表反向联查 |

`MultiTableRelationDemo` 参数依次为 `员工编号 [部门编号] [项目编号]`。不传参数时选择实际存在的员工及其关联部门、首个项目。本机样例员工编号是 `52～56`，示例参数为 `52 1 1`，编号以实际数据库为准。

多表查询同样排除已删除员工；使用 LEFT JOIN 和 `notNullColumn` 保留没有关联对象的主记录，并避免生成空员工/项目。`Emp.toString()` 打印基本信息，关联对象与集合由演示单独打印。

```bash
mvn -Dtest=MultiTableMapperTest test
```

四项测试验证全部关系、空关联、缺失记录、已删除员工过滤及删除后空集合。

## MyBatis-Plus：基础功能与七项补充

项目使用 MP 3.5.3.1。`EmpMapper extends BaseMapper<Emp>` 提供通用 CRUD、批量按编号操作、Wrapper、分页、selectMaps 和 selectByMap。`EmpQueryWrappers` 统一姓名、性别、部门、在职状态和最低薪资条件，按薪资降序、编号升序排序。

| 补充功能 | 适配员工实验的代码 | 验证方法（`EmpMapperAdvancedTest`） |
| --- | --- | --- |
| 自动填充 | `Emp.createTime/updateTime` + `MyMetaObjectHandler` | `autoFillCreatesAndRefreshesEmployeeTimes` |
| 逻辑删除 | `Emp.deleted` 的 `@TableLogic`；XML 及多表显式过滤 | `logicDeleteIsSeparateFromDepartureAndXmlAlsoFiltersDeletedRows` |
| 乐观锁 | `Emp.version` 的 `@Version` + 乐观锁插件 | `optimisticLockRejectsStaleSalaryAndXmlUpdateInvalidatesOldSnapshots` |
| 字段策略 | 岗位 `position/post` 的 `NOT_NULL`；创建时间禁止更新 | `notNullFieldStrategyRetainsPositionAndSupportsExplicitClear` |
| 部分字段 | `selectBriefRoster(StaffSearch)`：编号、姓名、部门、薪资；`selectMaps` | `partialFieldsAndMapsReturnEmployeeNameDepartmentAndSalary` |
| 分组聚合 | `summarizeDepartments()`：部门、人数、平均薪资 | `departmentGroupingCountsEmployeesAndAveragesSalary` |
| Map 查询 | `selectByMap`，键用 `emp_name`、`dept`、`status` 等数据库列名 | `mapEqualityUsesEmployeeColumnsAndFiltersDeletedRows` |

`EmpMapperPlusTest` 原有 5 项测试继续验证通用 CRUD、QueryWrapper/LambdaQueryWrapper、XML 与 Wrapper 更新共存、分页末页及空结果。`EmpMapperAdvancedTest` 新增 7 项测试对应上表；测试使用独立临时员工，结束后回滚。

```bash
mvn -Dtest=EmpMapperPlusTest,EmpMapperAdvancedTest test
```

IDEA 中运行整个 `EmpMapperAdvancedTest` 类观察七项功能；运行 `MyBatisPlusDemo.main()` 可直接看到条件查询、分页及七项功能的打印结果。

### 四步构建链路

现有 `MyBatisUtil` 共用 `mybatis-config.xml`，分别构建原生与 MP 工厂：`openClassicSession()` 使用原生构建器，`openSession()` 使用 MP 链路。

1. `MybatisXMLConfigBuilder` 解析 XML 得到 `MybatisConfiguration`。
2. 注册 `MybatisPlusConfig.buildInterceptor()`：乐观锁插件在前，分页插件在后。
3. 取得现有 `GlobalConfig`，注册 `MyMetaObjectHandler`。
4. 使用 `MybatisSqlSessionFactoryBuilder.build(configuration)` 构建 MP 工厂。

MP 与原生 MyBatis 的三个区别：MP 的配置/解析器/构建器支持通用 SQL 注入；分页与乐观锁需注册内部插件；自动填充需实体注解与 GlobalConfig 处理器共同生效。原生 XML 不依靠 MP 注解改写 SQL，因此它的过滤、版本和时间维护由 XML 显式实现。

乐观锁更新应读取带 `version` 的完整员工实体，或在更新对象中明确提供版本号。只提供编号、不提供版本的局部更新不会进行版本比较。两个旧版本快照需清除会话一级缓存后分别查询，避免两个变量指向同一对象。Wrapper 的自动填充需传入非 null 的实体；显式 `set(position, null)` 可清空岗位，普通 null 属性则按 NOT_NULL 保留原值。

### 日志与验证

`Preparing` 是参数化 SQL，`Parameters` 是实参与类型，`Total` 是返回行数，`Updates` 是影响行数。Mapper 包启用 DEBUG，其他日志使用 INFO。

关键 SQL 应包含：

```text
UPDATE emp SET deleted=1 WHERE emp_id=? AND deleted=0
UPDATE emp SET ... version=? WHERE emp_id=? AND version=? AND deleted=0
SELECT ... FROM emp WHERE deleted=0 AND ... LIMIT ...
SELECT dept, COUNT(*) AS employee_count, AVG(salary) AS average_salary FROM emp WHERE deleted=0 GROUP BY dept
```

过期版本更新应显示 `Updates: 0`。逻辑删除后 MP/XML 查询都不返回该员工；数据库物理行仍在。自动填充后创建/更新时间非 null，修改时创建时间保持原值。

完整验证使用 `mvn clean test`，清除旧模块的编译产物后运行全部测试。相关实现可对照 [MP 自动填充文档](https://baomidou.com/guides/auto-fill-field/) 与 [MP 乐观锁文档](https://baomidou.com/plugins/optimistic-locker/)。
