package cn.edu.course.mybatis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import cn.edu.course.mybatis.entity.Emp;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface EmployeeMapper extends BaseMapper<Emp> {
    List<Emp> findAll();

    Emp findById(Integer id);

    int save(Emp employee);

    int changeProfile(Emp employee);

    int removeById(Integer id);

    List<Emp> search(Emp filter);

    int patch(Emp employee);

    int removeMany(@Param("keys") List<Integer> ids);

    int addMany(@Param("roster") List<Emp> employees);

    List<Emp> chooseByNameOrDepartment(Emp query);
}
