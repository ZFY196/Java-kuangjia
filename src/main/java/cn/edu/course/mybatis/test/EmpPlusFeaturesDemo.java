package cn.edu.course.mybatis.test;

import cn.edu.course.mybatis.entity.Emp;
import cn.edu.course.mybatis.entity.StaffSearch;
import cn.edu.course.mybatis.mapper.EmpMapper;
import cn.edu.course.mybatis.util.MyBatisUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.session.SqlSession;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/** 员工七项补充功能演示，新增和修改均在结束时回滚。 */
public class EmpPlusFeaturesDemo {
    public static void main(String[] arguments) {
        try (SqlSession session = MyBatisUtil.openSession()) {
            try {
                EmpMapper mapper = session.getMapper(EmpMapper.class);
                System.out.println("\n== 1 自动填充：新增临时演示员工 ==");
                Emp temporary = new Emp();
                temporary.setName("MP功能演示员工");
                temporary.setSex("女");
                temporary.setDepartment("实验演示部");
                temporary.setPosition("开发工程师");
                temporary.setMonthlyPay(new BigDecimal("12000.00"));
                temporary.setJoinedOn(java.sql.Date.valueOf("2026-10-15"));
                temporary.setActiveFlag(1);
                mapper.insert(temporary);
                Emp current = mapper.selectById(temporary.getId());
                System.out.println("创建时间: " + current.getCreateTime() + "，更新时间: " + current.getUpdateTime());

                System.out.println("\n== 2 乐观锁：旧版本不能覆盖新薪资 ==");
                session.clearCache();
                Emp stale = mapper.selectById(current.getId());
                current.setMonthlyPay(new BigDecimal("14000.00"));
                System.out.println("当前版本更新影响行数: " + mapper.updateById(current));
                stale.setMonthlyPay(new BigDecimal("100.00"));
                System.out.println("过期版本更新影响行数: " + mapper.updateById(stale));
                Emp latest = mapper.selectById(current.getId());
                System.out.println("当前版本: " + latest.getVersion() + "，薪资: " + latest.getMonthlyPay());
                System.out.println("更新后自动填充时间: " + latest.getUpdateTime());

                System.out.println("\n== 3 字段策略：岗位null保留原值 ==");
                Emp patch = new Emp();
                patch.setId(current.getId());
                patch.setMonthlyPay(new BigDecimal("15000.00"));
                mapper.updateById(patch);
                System.out.println("只更新薪资后的岗位: " + mapper.selectById(current.getId()).getPosition());
                mapper.update(new Emp(), new LambdaUpdateWrapper<Emp>()
                        .eq(Emp::getId, current.getId()).set(Emp::getPosition, null));
                System.out.println("显式set(null)后的岗位: " + mapper.selectById(current.getId()).getPosition());

                System.out.println("\n== 4 部分字段查询：编号、姓名、部门、薪资 ==");
                mapper.selectBriefRoster(new StaffSearch()).forEach(employee -> System.out.printf(
                        "编号=%d，姓名=%s，部门=%s，薪资=%s%n", employee.getId(), employee.getName(),
                        employee.getDepartment(), employee.getMonthlyPay()));
                System.out.println("\n== 5 分组聚合：部门人数与平均薪资 ==");
                mapper.summarizeDepartments().forEach(System.out::println);
                System.out.println("\n== 6 Map等值查询：姓名与部门 ==");
                Map<String, Object> conditions = new HashMap<>();
                conditions.put("emp_name", temporary.getName());
                conditions.put("dept", temporary.getDepartment());
                conditions.put("emp_id", temporary.getId());
                mapper.selectByMap(conditions).forEach(System.out::println);
                System.out.println("\n== 7 逻辑删除：临时员工deleted变为1 ==");
                System.out.println("删除影响行数: " + mapper.deleteById(current.getId()));
                System.out.println("删除后按编号查询: " + mapper.selectById(current.getId()));
                System.out.println("删除后Map查询数量: " + mapper.selectByMap(conditions).size());
            } finally {
                session.rollback(true);
                System.out.println("\n演示事务已回滚，临时记录及修改均未保存。");
            }
        }
    }
}
