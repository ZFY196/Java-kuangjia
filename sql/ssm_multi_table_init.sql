-- 多表联合查询实验准备脚本（可重复执行），配合 ssm_emp_init.sql 使用。
-- 表关系设计：
--   dept 1:N emp        （emp.dept_id 外键指向 dept.deptno）
--   emp 1:1 idcard      （idcard.emp_id 唯一约束）
--   emp M:N project     （中间表 emp_project 承载两侧外键）
CREATE SCHEMA IF NOT EXISTS ssm_emp CHARACTER SET utf8mb4;
USE ssm_emp;

-- 部门表（与实体 cn.edu.course.mybatis.entity.Dept 字段对应）
CREATE TABLE IF NOT EXISTS dept (
    deptno INT NOT NULL AUTO_INCREMENT COMMENT '部门编号（主键）',
    dname VARCHAR(50) NOT NULL COMMENT '部门名称',
    loc VARCHAR(50) NULL COMMENT '部门所在城市',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标记，0未删除，1已删除',
    CONSTRAINT pk_dept PRIMARY KEY (deptno),
    CONSTRAINT uk_dept_name UNIQUE (dname)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

INSERT INTO dept (dname, loc)
SELECT samples.department_label, samples.city_label
FROM (
    SELECT '技术研发部' AS department_label, '上海' AS city_label
    UNION ALL SELECT '市场推广部', '杭州'
    UNION ALL SELECT '人力资源部', '苏州'
    UNION ALL SELECT '行政管理部', '青岛'
    UNION ALL SELECT '销售业务部', '北京'
) AS samples
WHERE NOT EXISTS (
    SELECT 1 FROM dept existing WHERE existing.dname = samples.department_label
);

-- 为 emp 表补充 dept_id 列（仅首次执行时 ALTER，重复执行自动跳过）
SET @dept_col = (SELECT COUNT(*) FROM information_schema.columns
                 WHERE table_schema = 'ssm_emp' AND table_name = 'emp' AND column_name = 'dept_id');
SET @dept_ddl = IF(@dept_col = 0,
    'ALTER TABLE emp ADD COLUMN dept_id INT NULL COMMENT ''所属部门编号（外键）'',
     ADD CONSTRAINT fk_emp_dept FOREIGN KEY (dept_id) REFERENCES dept(deptno)',
    'DO 0');
PREPARE stmt FROM @dept_ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 按部门名称回填 dept_id，建立一对多关联数据
UPDATE emp e JOIN dept d ON d.dname = e.dept SET e.dept_id = d.deptno
WHERE e.dept_id IS NULL;

-- 身份证表（与 emp 一对一）
CREATE TABLE IF NOT EXISTS idcard (
    idcard_id INT NOT NULL AUTO_INCREMENT COMMENT '证件编号（主键）',
    emp_id INT NOT NULL COMMENT '员工编号（唯一外键，一对一）',
    card_no VARCHAR(30) NOT NULL COMMENT '身份证号',
    expire_date DATE NULL COMMENT '证件有效期至',
    CONSTRAINT pk_idcard PRIMARY KEY (idcard_id),
    CONSTRAINT uk_idcard_emp UNIQUE (emp_id),
    CONSTRAINT fk_idcard_emp FOREIGN KEY (emp_id) REFERENCES emp (emp_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 为每名已有员工补发一张证件（已有则跳过）
INSERT INTO idcard (emp_id, card_no, expire_date)
SELECT e.emp_id, CONCAT('3101012024031', LPAD(e.emp_id, 4, '0'), 'X'),
       DATE_ADD(e.hire_date, INTERVAL 10 YEAR)
FROM emp e
WHERE NOT EXISTS (SELECT 1 FROM idcard i WHERE i.emp_id = e.emp_id);

-- 项目表与员工-项目关联表（多对多）
CREATE TABLE IF NOT EXISTS project (
    project_id INT NOT NULL AUTO_INCREMENT COMMENT '项目编号（主键）',
    project_name VARCHAR(50) NOT NULL COMMENT '项目名称',
    start_date DATE NULL COMMENT '项目启动日期',
    CONSTRAINT pk_project PRIMARY KEY (project_id),
    CONSTRAINT uk_project_name UNIQUE (project_name)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

INSERT INTO project (project_name, start_date)
SELECT samples.project_label, samples.begin_date
FROM (
    SELECT '智慧校园平台' AS project_label, CAST('2025-01-06' AS DATE) AS begin_date
    UNION ALL SELECT '移动端商城', '2025-04-01'
    UNION ALL SELECT '经营数据看板', '2025-07-15'
) AS samples
WHERE NOT EXISTS (
    SELECT 1 FROM project existing WHERE existing.project_name = samples.project_label
);

CREATE TABLE IF NOT EXISTS emp_project (
    emp_id INT NOT NULL COMMENT '员工编号',
    project_id INT NOT NULL COMMENT '项目编号',
    CONSTRAINT pk_emp_project PRIMARY KEY (emp_id, project_id),
    CONSTRAINT fk_ep_emp FOREIGN KEY (emp_id) REFERENCES emp (emp_id),
    CONSTRAINT fk_ep_project FOREIGN KEY (project_id) REFERENCES project (project_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 按姓名分配项目成员（一名员工可参与多个项目，一个项目可有多名员工）
INSERT INTO emp_project (emp_id, project_id)
SELECT e.emp_id, p.project_id
FROM emp e
JOIN project p ON (
       (p.project_name = '智慧校园平台' AND e.emp_name IN ('张明远', '王浩然', '陈思语'))
    OR (p.project_name = '移动端商城'   AND e.emp_name IN ('李欣怡', '陈思语', '赵文博'))
    OR (p.project_name = '经营数据看板' AND e.emp_name IN ('张明远', '李欣怡'))
)
WHERE NOT EXISTS (
    SELECT 1 FROM emp_project linked
    WHERE linked.emp_id = e.emp_id AND linked.project_id = p.project_id
);
