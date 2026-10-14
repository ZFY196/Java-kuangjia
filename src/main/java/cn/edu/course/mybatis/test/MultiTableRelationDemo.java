package cn.edu.course.mybatis.test;

import cn.edu.course.mybatis.entity.Dept;
import cn.edu.course.mybatis.entity.Emp;
import cn.edu.course.mybatis.entity.Project;
import cn.edu.course.mybatis.mapper.MultiTableMapper;
import cn.edu.course.mybatis.mapper.EmpMapper;
import cn.edu.course.mybatis.util.MyBatisUtil;
import org.apache.ibatis.session.SqlSession;
import java.util.List;

/**
 * 多表联合查询演示：一对一、多对一、一对多、双向多对多。
 *
 * 运行前请先执行 sql/ssm_multi_table_init.sql 建表并灌入样例数据。
 * 可通过命令行参数指定查询编号：员工编号 [部门编号] [项目编号]。
 * 未指定时选取实际存在的员工，以及该员工关联的部门和首个项目。
 */
public class MultiTableRelationDemo {
    public static void main(String[] arguments) {
        try (SqlSession session = MyBatisUtil.openClassicSession()) {
            MultiTableMapper repository = session.getMapper(MultiTableMapper.class);
            Integer empKey;
            if (arguments.length >= 1) {
                empKey = Integer.valueOf(arguments[0]);
            } else {
                List<Emp> roster = session.getMapper(EmpMapper.class).listRoster();
                if (roster.isEmpty()) {
                    System.out.println("暂无员工，请先执行 sql/ssm_emp_init.sql，再执行 sql/ssm_multi_table_init.sql。");
                    return;
                }
                empKey = roster.get(0).getId();
            }
            Emp withDept = repository.findEmpWithDept(empKey);
            Emp withProjects = repository.findEmpWithProjects(empKey);
            Integer deptKey = arguments.length >= 2 ? Integer.valueOf(arguments[1])
                    : withDept != null && withDept.getDeptInfo() != null
                    ? withDept.getDeptInfo().getDeptno() : null;
            Integer projectKey = arguments.length >= 3 ? Integer.valueOf(arguments[2])
                    : withProjects != null && !withProjects.getProjects().isEmpty()
                    ? withProjects.getProjects().get(0).getProjectId() : null;

            System.out.println("== 一对一：员工 及其 身份证（emp_id=" + empKey + "）==");
            Emp withCard = repository.findEmpWithIdCard(empKey);
            System.out.println(withCard == null ? "员工不存在" : withCard);
            if (withCard != null) {
                System.out.println("  证件 → " + (withCard.getIdCard() == null ? "未关联身份证" : withCard.getIdCard()));
            }

            System.out.println("\n== 多对一：员工 及其 所属部门（emp_id=" + empKey + "）==");
            if (withDept == null) {
                System.out.println("员工不存在");
            } else {
                System.out.println(withDept);
                System.out.println("  所属部门 → " + (withDept.getDeptInfo() == null ? "未关联有效部门" : withDept.getDeptInfo()));
            }

            System.out.println("\n== 一对多：部门 及其 员工列表（deptno=" + deptKey + "）==");
            Dept department = deptKey == null ? null : repository.findDeptWithEmps(deptKey);
            if (deptKey == null) {
                System.out.println("该员工未关联有效部门，请通过第二个参数指定部门编号");
            } else if (department == null) {
                System.out.println("部门不存在");
            } else {
                System.out.println(department);
                if (department.getEmps().isEmpty()) {
                    System.out.println("  暂无员工");
                }
                for (Emp member : department.getEmps()) {
                    System.out.println("  成员 → " + member);
                }
            }

            System.out.println("\n== 多对多·方向一：通过项目查员工（project_id=" + projectKey + "）==");
            Project project = projectKey == null ? null : repository.findProjectWithEmps(projectKey);
            if (projectKey == null) {
                System.out.println("该员工暂未参与项目，请通过第三个参数指定项目编号");
            } else if (project == null) {
                System.out.println("项目不存在");
            } else {
                System.out.println(project);
                if (project.getEmps().isEmpty()) {
                    System.out.println("  暂无参与员工");
                }
                for (Emp member : project.getEmps()) {
                    System.out.println("  参与员工 → " + member);
                }
            }

            System.out.println("\n== 多对多·方向二：通过员工查项目（emp_id=" + empKey + "）==");
            if (withProjects == null) {
                System.out.println("员工不存在");
            } else {
                System.out.println(withProjects);
                if (withProjects.getProjects().isEmpty()) {
                    System.out.println("  暂未参与项目");
                }
                for (Project joined : withProjects.getProjects()) {
                    System.out.println("  参与项目 → " + joined);
                }
            }
        }
    }
}
