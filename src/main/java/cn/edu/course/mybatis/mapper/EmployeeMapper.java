package cn.edu.course.mybatis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import cn.edu.course.mybatis.domain.Employee;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface EmployeeMapper extends BaseMapper<Employee> {
    List<Employee> findAll();

    Employee findById(Integer id);

    int save(Employee employee);

    int changeProfile(Employee employee);

    int removeById(Integer id);

    List<Employee> search(Employee filter);

    int patch(Employee employee);

    int removeMany(@Param("keys") List<Integer> ids);

    int addMany(@Param("roster") List<Employee> employees);

    List<Employee> chooseByNameOrDepartment(Employee query);
}
