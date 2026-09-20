CREATE DATABASE IF NOT EXISTS ssm_emp DEFAULT CHARACTER SET utf8mb4;
USE ssm_emp;

DROP TABLE IF EXISTS emp;
CREATE TABLE emp (
    emp_id     INT PRIMARY KEY AUTO_INCREMENT COMMENT '员工编号',
    emp_name   VARCHAR(50)  NOT NULL COMMENT '姓名',
    gender     CHAR(1)      DEFAULT '男' COMMENT '性别',
    dept       VARCHAR(50)  COMMENT '部门',
    post       VARCHAR(50)  COMMENT '岗位',
    salary     DECIMAL(10,2) COMMENT '薪资',
    hire_date  DATE         COMMENT '入职时间',
    status     TINYINT      DEFAULT 1 COMMENT '状态：1在职 0离职'
);

INSERT INTO emp (emp_name, gender, dept, post, salary, hire_date, status) VALUES
('周屿', '男', '平台研发部', '后端开发工程师', 13800.00, '2025-02-18', 1),
('林若溪', '女', '产品运营部', '产品助理', 8600.00, '2024-11-04', 1),
('许承安', '男', '数据智能部', '数据分析师', 11200.00, '2023-09-12', 1),
('唐雨晴', '女', '质量保障部', '自动化测试工程师', 9700.00, '2024-05-23', 1),
('高铭', '男', '客户成功部', '实施顾问', 9100.00, '2022-12-01', 0);
