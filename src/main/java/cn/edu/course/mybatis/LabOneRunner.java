package cn.edu.course.mybatis;

import cn.edu.course.mybatis.domain.Employee;
import cn.edu.course.mybatis.mapper.EmployeeMapper;
import cn.edu.course.mybatis.support.DatabaseBootstrap;
import cn.edu.course.mybatis.support.MyBatisSessions;
import org.apache.ibatis.session.SqlSession;

public class LabOneRunner {
    public static void main(String[] args) {
        DatabaseBootstrap.ensureReady();
        try (SqlSession session = MyBatisSessions.open()) {
            EmployeeMapper employees = session.getMapper(EmployeeMapper.class);
            for (Employee employee : employees.findAll()) {
                System.out.println(employee);
            }
        }
    }
}
