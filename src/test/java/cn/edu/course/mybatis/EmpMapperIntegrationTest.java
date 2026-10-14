package cn.edu.course.mybatis;

import cn.edu.course.mybatis.entity.Emp;
import cn.edu.course.mybatis.mapper.EmpMapper;
import cn.edu.course.mybatis.util.MyBatisUtil;
import org.apache.ibatis.session.SqlSession;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Date;

import static org.junit.Assert.*;

public class EmpMapperIntegrationTest {
    @Test
    public void crudMapsAllFieldsAndReturnsGeneratedKey() {
        try (SqlSession session = MyBatisUtil.openSession()) {
            try {
                EmpMapper mapper = session.getMapper(EmpMapper.class);
                int originalCount = mapper.listRoster().size();
                Emp employee = new Emp();
                employee.setName("映射测试员工");
                employee.setSex("女");
                employee.setDepartment("测试部");
                employee.setPosition("测试工程师");
                employee.setMonthlyPay(new BigDecimal("8800.50"));
                employee.setJoinedOn(Date.valueOf("2026-10-14"));
                employee.setActiveFlag(1);

                assertEquals(1, mapper.insert(employee));
                assertNotNull(employee.getId());
                assertEquals(originalCount + 1, mapper.listRoster().size());
                assertMappedFields(employee, mapper.selectById(employee.getId()));

                employee.setName("更新后的测试员工");
                employee.setSex("男");
                employee.setDepartment("质量保障部");
                employee.setPosition("测试主管");
                employee.setMonthlyPay(new BigDecimal("10500.75"));
                employee.setActiveFlag(0);
                assertEquals(1, mapper.replaceProfile(employee));
                assertMappedFields(employee, mapper.selectById(employee.getId()));

                assertEquals(1, mapper.deleteById(employee.getId()));
                assertNull(mapper.selectById(employee.getId()));
                assertEquals(originalCount, mapper.listRoster().size());
            } finally {
                session.rollback();
            }
        }
    }

    @Test
    public void rollbackDoesNotPersistInsertedEmployee() {
        Integer employeeId;
        try (SqlSession session = MyBatisUtil.openSession()) {
            Emp employee = new Emp();
            employee.setName("事务回滚测试员工");
            employee.setSex("女");
            employee.setDepartment("测试部");
            employee.setPosition("测试工程师");
            employee.setMonthlyPay(new BigDecimal("8800.00"));
            employee.setJoinedOn(Date.valueOf("2026-10-14"));
            employee.setActiveFlag(1);
            assertEquals(1, session.getMapper(EmpMapper.class).insert(employee));
            employeeId = employee.getId();
            assertNotNull(employeeId);
            session.rollback();
        }
        try (SqlSession session = MyBatisUtil.openSession()) {
            assertNull(session.getMapper(EmpMapper.class).selectById(employeeId));
        }
    }

    private static void assertMappedFields(Emp expected, Emp actual) {
        assertNotNull(actual);
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getSex(), actual.getSex());
        assertEquals(expected.getDepartment(), actual.getDepartment());
        assertEquals(expected.getPosition(), actual.getPosition());
        assertEquals(expected.getMonthlyPay(), actual.getMonthlyPay());
        assertEquals(expected.getJoinedOn().getTime(), actual.getJoinedOn().getTime());
        assertEquals(expected.getActiveFlag(), actual.getActiveFlag());
    }
}
