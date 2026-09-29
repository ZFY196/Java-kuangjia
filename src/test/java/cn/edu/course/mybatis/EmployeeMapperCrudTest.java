package cn.edu.course.mybatis;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.edu.course.mybatis.domain.Employee;
import cn.edu.course.mybatis.mapper.EmployeeMapper;
import cn.edu.course.mybatis.support.DatabaseBootstrap;
import cn.edu.course.mybatis.support.MyBatisSessions;
import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;
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

    @Test
    public void dynamicSearchCombinesOnlyProvidedFields() {
        try (SqlSession session = MyBatisSessions.open()) {
            EmployeeMapper mapper = session.getMapper(EmployeeMapper.class);
            Employee employee = EmployeeSamples.newSupportEngineer();
            mapper.save(employee);

            Employee filter = new Employee();
            filter.setName("嘉禾");
            filter.setDepartment("技术支持部");
            filter.setActive(1);
            List<Employee> matched = mapper.search(filter);
            session.rollback();

            Assert.assertEquals(1, matched.size());
            Assert.assertEquals("交付工程师", matched.get(0).getJobTitle());
        }
    }

    @Test
    public void patchUpdatesOnlyNonNullColumns() {
        try (SqlSession session = MyBatisSessions.open()) {
            EmployeeMapper mapper = session.getMapper(EmployeeMapper.class);
            Employee employee = EmployeeSamples.newSupportEngineer();
            mapper.save(employee);

            Employee patch = new Employee();
            patch.setId(employee.getId());
            patch.setJobTitle("实施顾问");
            patch.setSalary(new BigDecimal("11120.00"));
            int rows = mapper.patch(patch);
            Employee changed = mapper.findById(employee.getId());
            session.rollback();

            Assert.assertEquals(1, rows);
            Assert.assertEquals("技术支持部", changed.getDepartment());
            Assert.assertEquals("实施顾问", changed.getJobTitle());
        }
    }

    @Test
    public void batchInsertAndDeleteUseForeachParameters() {
        try (SqlSession session = MyBatisSessions.open()) {
            EmployeeMapper mapper = session.getMapper(EmployeeMapper.class);
            Employee first = EmployeeSamples.newFinanceAnalyst("周雨晴");
            Employee second = EmployeeSamples.newFinanceAnalyst("何清妍");

            int inserted = mapper.addMany(Arrays.asList(first, second));
            Employee filter = new Employee();
            filter.setDepartment("财务共享中心");
            List<Employee> added = mapper.search(filter);
            int deleted = mapper.removeMany(Arrays.asList(added.get(0).getId(), added.get(1).getId()));
            session.rollback();

            Assert.assertEquals(2, inserted);
            Assert.assertTrue(added.size() >= 2);
            Assert.assertEquals(2, deleted);
        }
    }

    @Test
    public void chooseQueryPrefersNameBeforeDepartment() {
        try (SqlSession session = MyBatisSessions.open()) {
            EmployeeMapper mapper = session.getMapper(EmployeeMapper.class);
            Employee engineer = EmployeeSamples.newSupportEngineer();
            Employee analyst = EmployeeSamples.newFinanceAnalyst("顾晨曦");
            mapper.save(engineer);
            mapper.save(analyst);

            Employee query = new Employee();
            query.setName("沈嘉禾");
            query.setDepartment("财务共享中心");
            List<Employee> picked = mapper.chooseByNameOrDepartment(query);
            session.rollback();

            Assert.assertEquals(1, picked.size());
            Assert.assertEquals("技术支持部", picked.get(0).getDepartment());
        }
    }

    @Test
    public void myBatisPlusBaseMapperCrudWorksWithAnnotatedEntity() {
        try (SqlSession session = MyBatisSessions.open()) {
            EmployeeMapper mapper = session.getMapper(EmployeeMapper.class);
            Employee employee = EmployeeSamples.newFinanceAnalyst("宋知夏");

            int rows = mapper.insert(employee);
            Employee loaded = mapper.selectById(employee.getId());
            session.rollback();

            Assert.assertEquals(1, rows);
            Assert.assertEquals("财务共享中心", loaded.getDepartment());
        }
    }

    @Test
    public void myBatisPlusWrapperAndPageReturnActiveEmployees() {
        try (SqlSession session = MyBatisSessions.open()) {
            EmployeeMapper mapper = session.getMapper(EmployeeMapper.class);
            LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<Employee>()
                    .eq(Employee::getActive, 1)
                    .orderByDesc(Employee::getSalary);

            Page<Employee> result = mapper.selectPage(new Page<Employee>(1, 2), wrapper);

            Assert.assertTrue(result.getRecords().size() <= 2);
            Assert.assertTrue(result.getRecords().stream().allMatch(e -> Integer.valueOf(1).equals(e.getActive())));
        }
    }
}
