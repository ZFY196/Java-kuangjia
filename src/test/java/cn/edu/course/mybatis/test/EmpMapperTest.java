package cn.edu.course.mybatis.test;

import cn.edu.course.mybatis.entity.Emp;
import cn.edu.course.mybatis.mapper.EmpMapper;
import cn.edu.course.mybatis.util.MyBatisUtil;
import org.apache.ibatis.session.SqlSession;
import org.junit.After;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class EmpMapperTest {
    private Integer temporaryKey;

    @Test
    public void testInsert() {
        Emp employee = newEmployee();
        try (SqlSession session = MyBatisUtil.openClassicSession()) {
            int rows = session.getMapper(EmpMapper.class).createRecord(employee);
            temporaryKey = employee.getId();
            session.commit();

            assertEquals(1, rows);
            assertNotNull("新增后应回填自增主键", temporaryKey);
            System.out.println("新增员工“沈知秋”，回填编号 id = " + temporaryKey);
        }

        // 在新会话中查询，确认提交后的记录已保存到数据库。
        try (SqlSession session = MyBatisUtil.openClassicSession()) {
            Emp saved = session.getMapper(EmpMapper.class).findRecord(temporaryKey);
            assertNotNull(saved);
            assertEquals("沈知秋", saved.getName());
            assertEquals("女", saved.getSex());
            assertEquals("数据平台部", saved.getDepartment());
            assertEquals("自动化测试工程师", saved.getPosition());
            assertEquals(0, new BigDecimal("11860.00").compareTo(saved.getMonthlyPay()));
            assertEquals(Integer.valueOf(1), saved.getActiveFlag());
        }
    }

    @Test
    public void testUpdate() {
        insertFixture();
        try (SqlSession session = MyBatisUtil.openClassicSession()) {
            EmpMapper mapper = session.getMapper(EmpMapper.class);
            Emp employee = mapper.findRecord(temporaryKey);
            assertNotNull(employee);
            employee.setMonthlyPay(new BigDecimal("14680.00"));

            int rows = mapper.replaceProfile(employee);
            session.commit();
            assertEquals(1, rows);
        }

        try (SqlSession session = MyBatisUtil.openClassicSession()) {
            Emp updated = session.getMapper(EmpMapper.class).findRecord(temporaryKey);
            assertNotNull(updated);
            assertEquals(0, new BigDecimal("14680.00").compareTo(updated.getMonthlyPay()));
            assertEquals("沈知秋", updated.getName());
        }
    }

    @Test
    public void testDelete() {
        insertFixture();
        try (SqlSession session = MyBatisUtil.openClassicSession()) {
            int rows = session.getMapper(EmpMapper.class).removeRecord(temporaryKey);
            session.commit();
            assertEquals(1, rows);
        }

        try (SqlSession session = MyBatisUtil.openClassicSession()) {
            assertNull(session.getMapper(EmpMapper.class).findRecord(temporaryKey));
        }
    }

    @After
    public void cleanUpTestEmployee() throws java.sql.SQLException {
        if (temporaryKey != null) {
            try (SqlSession session = MyBatisUtil.openClassicSession()) {
                // 清理仅由本测试创建的记录；业务删除仍通过逻辑删除执行。
                try (java.sql.PreparedStatement statement = session.getConnection()
                        .prepareStatement("DELETE FROM emp WHERE emp_id = ?")) {
                    statement.setInt(1, temporaryKey);
                    statement.executeUpdate();
                    session.commit(true);
                }
            }
        }
    }

    private void insertFixture() {
        Emp employee = newEmployee();
        try (SqlSession session = MyBatisUtil.openClassicSession()) {
            int rows = session.getMapper(EmpMapper.class).createRecord(employee);
            temporaryKey = employee.getId();
            session.commit();
            assertEquals(1, rows);
            assertNotNull(temporaryKey);
        }
    }

    private Emp newEmployee() {
        Emp employee = new Emp();
        employee.setName("沈知秋");
        employee.setSex("女");
        employee.setDepartment("数据平台部");
        employee.setPosition("自动化测试工程师");
        employee.setMonthlyPay(new BigDecimal("11860.00"));
        employee.setJoinedOn(new Date());
        employee.setActiveFlag(1);
        return employee;
    }
}
