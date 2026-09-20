package cn.edu.course.mybatis.mapper;

import cn.edu.course.mybatis.domain.Employee;

import java.util.List;

public interface EmployeeMapper {
    List<Employee> findAll();

    Employee findById(Integer id);

    int save(Employee employee);

    int changeProfile(Employee employee);

    int removeById(Integer id);
}
