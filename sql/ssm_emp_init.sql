-- 可重复执行的实验准备脚本；已有员工保留，同名样例不会再次加入。
CREATE SCHEMA IF NOT EXISTS ssm_emp CHARACTER SET utf8mb4;
USE ssm_emp;

CREATE TABLE IF NOT EXISTS emp (
    emp_id INTEGER NOT NULL AUTO_INCREMENT,
    emp_name VARCHAR(50) NOT NULL COMMENT '员工显示姓名',
    salary DECIMAL(10, 2) NULL COMMENT '月度薪资金额',
    hire_date DATE NULL COMMENT '加入公司日期',
    status TINYINT DEFAULT 1 COMMENT '就业状态，1在职，0离职',
    dept VARCHAR(50) NULL COMMENT '所属组织单元',
    post VARCHAR(50) NULL COMMENT '工作职责名称',
    gender CHAR(1) DEFAULT '男' COMMENT '性别标签',
    create_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
    update_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除，0正常1删除，与status独立',
    version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    CONSTRAINT pk_employee PRIMARY KEY (emp_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 升级已存在的员工表：按缺失列构造 ALTER，重复执行不丢失数据。
SET @emp_upgrade = (
    SELECT GROUP_CONCAT(required.ddl ORDER BY required.ordinal SEPARATOR ', ')
    FROM (
        SELECT 1 AS ordinal, 'create_time' AS column_name,
               'ADD COLUMN create_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)' AS ddl
        UNION ALL SELECT 2, 'update_time',
               'ADD COLUMN update_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)'
        UNION ALL SELECT 3, 'deleted', 'ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0'
        UNION ALL SELECT 4, 'version', 'ADD COLUMN version INT NOT NULL DEFAULT 0'
    ) required
    WHERE NOT EXISTS (
        SELECT 1 FROM information_schema.columns existing
        WHERE existing.table_schema = 'ssm_emp' AND existing.table_name = 'emp'
              AND existing.column_name = required.column_name
    )
);
SET @emp_upgrade_sql = IF(@emp_upgrade IS NULL, 'DO 0', CONCAT('ALTER TABLE emp ', @emp_upgrade));
PREPARE emp_upgrade_statement FROM @emp_upgrade_sql;
EXECUTE emp_upgrade_statement;
DEALLOCATE PREPARE emp_upgrade_statement;

INSERT INTO emp (emp_name, salary, hire_date, status, dept, post, gender)
SELECT samples.full_name, samples.monthly_amount, samples.start_date,
       samples.is_active, samples.department_label, samples.role_label, samples.sex_label
FROM (
    SELECT '张明远' AS full_name, 15600.00 AS monthly_amount,
           CAST('2024-03-15' AS DATE) AS start_date, 1 AS is_active,
           '技术研发部' AS department_label, '软件工程师' AS role_label, '男' AS sex_label
    UNION ALL SELECT '李欣怡', 10200.00, '2025-06-20', 1, '市场推广部', '品牌策划师', '女'
    UNION ALL SELECT '王浩然', 12400.00, '2023-08-10', 1, '人力资源部', '招聘主管', '男'
    UNION ALL SELECT '陈思语', 7800.00, '2025-09-01', 1, '行政管理部', '行政专员', '女'
    UNION ALL SELECT '赵文博', 13500.00, '2022-04-18', 0, '销售业务部', '客户经理', '男'
) AS samples
WHERE NOT EXISTS (
    SELECT 1 FROM emp registered WHERE registered.emp_name = samples.full_name
);
