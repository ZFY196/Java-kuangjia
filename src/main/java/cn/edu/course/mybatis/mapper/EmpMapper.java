package cn.edu.course.mybatis.mapper;

import cn.edu.course.mybatis.entity.Emp;
import cn.edu.course.mybatis.entity.StaffSearch;
import cn.edu.course.mybatis.util.EmpQueryWrappers;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Collection;
import java.util.Map;

public interface EmpMapper extends BaseMapper<Emp> {
    List<Emp> listRoster();

    Emp findRecord(@Param("key") Integer employeeKey);

    int createRecord(@Param("record") Emp employee);

    int removeRecord(@Param("key") Integer employeeKey);

    int replaceProfile(@Param("record") Emp employee);

    List<Emp> searchRoster(@Param("criteria") StaffSearch request);

    List<Emp> searchRosterWithTrim(@Param("criteria") StaffSearch request);

    int patchProfile(@Param("change") Emp changes);

    int removeRoster(@Param("employeeKeys") Collection<Integer> keys);

    int addRoster(@Param("newcomers") List<Emp> records);

    List<Emp> findPreferred(@Param("criteria") StaffSearch request);

    /** 部分字段查询：只返回编号、姓名、部门和薪资，沿用原员工筛选条件。 */
    default List<Emp> selectBriefRoster(StaffSearch request) {
        return selectList(EmpQueryWrappers.byProperties(request)
                .select(Emp::getId, Emp::getName, Emp::getDepartment, Emp::getMonthlyPay));
    }

    /** 按部门统计正常员工人数与平均薪资；离职记录可由 status 条件单独筛选。 */
    default List<Map<String, Object>> summarizeDepartments() {
        return selectMaps(new QueryWrapper<Emp>()
                .select("dept", "COUNT(*) AS employee_count", "AVG(salary) AS average_salary")
                .groupBy("dept").orderByAsc("dept"));
    }
}
