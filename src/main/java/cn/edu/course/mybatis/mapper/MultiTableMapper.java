package cn.edu.course.mybatis.mapper;

import cn.edu.course.mybatis.entity.Dept;
import cn.edu.course.mybatis.entity.Emp;
import cn.edu.course.mybatis.entity.Project;
import org.apache.ibatis.annotations.Param;

/**
 * 多表联合查询 Mapper（XML 实现，见 resources 下同名 MultiTableMapper.xml）
 *
 * 覆盖以下关联查询：
 *   一对一：emp 与 idcard（association 嵌套）
 *   一对多：dept 与 emp（collection 嵌套）
 *   多对一：emp 与 dept（association 嵌套）
 *   多对多：project 与 emp（正向：通过项目查员工）
 *   多对多：emp 与 project（反向：通过员工查项目）
 */
public interface MultiTableMapper {

    /** 一对一：按员工编号查询员工及其身份证信息 */
    Emp findEmpWithIdCard(@Param("empId") Integer empId);

    /** 多对一：按员工编号查询员工及其所属部门 */
    Emp findEmpWithDept(@Param("empId") Integer empId);

    /** 一对多：按部门编号查询部门及其全部员工 */
    Dept findDeptWithEmps(@Param("deptno") Integer deptno);

    /** 多对多（方向一）：按项目编号查询项目及其全部参与员工 */
    Project findProjectWithEmps(@Param("projectId") Integer projectId);

    /** 多对多（方向二）：按员工编号查询员工及其参与的全部项目 */
    Emp findEmpWithProjects(@Param("empId") Integer empId);
}
