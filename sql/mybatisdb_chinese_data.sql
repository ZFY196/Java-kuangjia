-- 将现有示例数据中文化，保留编号、金额、日期及部门关联。
-- 中国城市随机选取后固定，重复执行不会再次改变数据。
SET NAMES utf8mb4;
USE mybatisdb;

START TRANSACTION;

UPDATE emp
SET ename = CASE ename
    WHEN 'SMITH' THEN '张明远'
    WHEN 'ALLEN' THEN '李浩然'
    WHEN 'WARD' THEN '王志强'
    WHEN 'JONES' THEN '陈建国'
    WHEN 'MARTIN' THEN '赵文博'
    WHEN 'BLAKE' THEN '刘思远'
    WHEN 'CLARK' THEN '黄俊杰'
    WHEN 'SCOTT' THEN '周睿'
    WHEN 'KING' THEN '吴国华'
    WHEN 'TURNER' THEN '徐嘉诚'
    WHEN 'ADAMS' THEN '孙欣怡'
    WHEN 'JAMES' THEN '胡雅婷'
    WHEN 'FORD' THEN '朱子涵'
    WHEN 'MILLER' THEN '林思语'
    ELSE ename
END,
job = CASE job
    WHEN 'CLERK' THEN '文员'
    WHEN 'SALESMAN' THEN '销售员'
    WHEN 'MANAGER' THEN '经理'
    WHEN 'ANALYST' THEN '分析师'
    WHEN 'PRESIDENT' THEN '总裁'
    ELSE job
END;

UPDATE dept
SET dname = CASE dname
    WHEN 'ACCOUNTING' THEN '财务部'
    WHEN 'RESEARCH' THEN '研发部'
    WHEN 'SALES' THEN '销售部'
    WHEN 'OPERATIONS' THEN '运营部'
    ELSE dname
END,
loc = CASE loc
    WHEN 'NEW YORK' THEN '上海'
    WHEN 'DALLAS' THEN '苏州'
    WHEN 'CHICAGO' THEN '杭州'
    WHEN 'BOSTON' THEN '青岛'
    ELSE loc
END;

COMMIT;
