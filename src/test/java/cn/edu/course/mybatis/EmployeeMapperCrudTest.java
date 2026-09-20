package cn.edu.course.mybatis;

import cn.edu.course.mybatis.domain.Employee;
import cn.edu.course.mybatis.mapper.EmployeeMapper;
import cn.edu.course.mybatis.support.DatabaseBootstrap;
import cn.edu.course.mybatis.support.MyBatisSessions;
import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

public class EmployeeMapperCrudTest {
    @BeforeClass
    public static void prepareDatabase() {
        DatabaseBootstrap.ensureReady();
    }

    @Test
    public void listEmployeesReturnsRowsFromEmpTable() {
        try (SqlSession session = MyBatisSessions.open()) {
            List<Employee> employees = session.getMapper(EmployeeMapper.class).findAll();

            Assert.assertFalse("请先执行 sql/ssm_emp_init.sql 初始化数据", employees.isEmpty());
        }
    }

    @Test
    public void insertedEmployeeCanBeLoadedByGeneratedId() {
        try (SqlSession session = MyBatisSessions.open()) {
            EmployeeMapper mapper = session.getMapper(EmployeeMapper.class);
            Employee created = EmployeeSamples.newSupportEngineer();

            int rows = mapper.save(created);
            Employee loaded = mapper.findById(created.getId());
            session.rollback();

            Assert.assertEquals(1, rows);
            Assert.assertNotNull(created.getId());
            Assert.assertEquals("沈嘉禾", loaded.getName());
        }
    }

    @Test
    public void updateChangesTheEmployeeRecord() {
        try (SqlSession session = MyBatisSessions.open()) {
            EmployeeMapper mapper = session.getMapper(EmployeeMapper.class);
            Employee employee = EmployeeSamples.newSupportEngineer();
            mapper.save(employee);

            employee.setDepartment("平台服务部");
            employee.setJobTitle("客户平台工程师");
            employee.setSalary(new BigDecimal("11880.00"));
            int rows = mapper.changeProfile(employee);
            Employee changed = mapper.findById(employee.getId());
            session.rollback();

            Assert.assertEquals(1, rows);
            Assert.assertEquals("平台服务部", changed.getDepartment());
            Assert.assertEquals(new BigDecimal("11880.00"), changed.getSalary());
        }
    }

    @Test
    public void deleteRemovesOnlyTheSelectedEmployee() {
        try (SqlSession session = MyBatisSessions.open()) {
            EmployeeMapper mapper = session.getMapper(EmployeeMapper.class);
            Employee employee = EmployeeSamples.newSupportEngineer();
            mapper.save(employee);

            int rows = mapper.removeById(employee.getId());
            Employee deleted = mapper.findById(employee.getId());
            session.rollback();

            Assert.assertEquals(1, rows);
            Assert.assertNull(deleted);
        }
    }
}
