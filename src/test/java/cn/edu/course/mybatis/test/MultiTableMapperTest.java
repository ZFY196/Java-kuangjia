package cn.edu.course.mybatis.test;

import cn.edu.course.mybatis.entity.Dept;
import cn.edu.course.mybatis.entity.Emp;
import cn.edu.course.mybatis.entity.Project;
import cn.edu.course.mybatis.mapper.MultiTableMapper;
import cn.edu.course.mybatis.util.MyBatisUtil;
import org.apache.ibatis.session.SqlSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/** 在独立事务中创建关联数据，结束后回滚，不依赖已有样例的编号。 */
public class MultiTableMapperTest {
    private SqlSession session;
    private MultiTableMapper mapper;
    private int deptId;
    private int firstEmp;
    private int secondEmp;
    private int firstProject;
    private int secondProject;

    @Before
    public void setUp() throws SQLException {
        session = MyBatisUtil.openClassicSession();
        mapper = session.getMapper(MultiTableMapper.class);
        try {
            String suffix = Long.toString(System.nanoTime());
            deptId = insert("INSERT INTO dept(dname, loc, deleted) VALUES (?, '上海', 0)", "关联测试部" + suffix);
            firstEmp = insert("INSERT INTO emp(emp_name, gender, dept, dept_id, status) VALUES (?, '女', ?, ?, 1)",
                    "关联测试员工甲", "关联测试部" + suffix, deptId);
            secondEmp = insert("INSERT INTO emp(emp_name, gender, dept, dept_id, status) VALUES (?, '男', ?, ?, 1)",
                    "关联测试员工乙", "关联测试部" + suffix, deptId);
            firstProject = insert("INSERT INTO project(project_name, start_date) VALUES (?, '2026-10-15')", "关联测试项目甲" + suffix);
            secondProject = insert("INSERT INTO project(project_name, start_date) VALUES (?, '2026-10-15')", "关联测试项目乙" + suffix);
            insert("INSERT INTO idcard(emp_id, card_no, expire_date) VALUES (?, ?, '2036-10-15')", firstEmp, "TEST-" + suffix);
            execute("INSERT INTO emp_project(emp_id, project_id) VALUES (?, ?), (?, ?), (?, ?)",
                    firstEmp, firstProject, firstEmp, secondProject, secondEmp, firstProject);
        } catch (SQLException exception) {
            tearDown();
            throw exception;
        }
    }

    @After
    public void tearDown() {
        if (session != null) {
            try {
                // 数据通过 JDBC 写入，强制回滚以覆盖 MyBatis 未标记为 dirty 的会话。
                session.rollback(true);
            } finally {
                session.close();
                session = null;
            }
        }
    }

    @Test
    public void mapsAllRelationsAndAggregatesEachCollection() {
        Emp withCard = mapper.findEmpWithIdCard(firstEmp);
        assertEquals("关联测试员工甲", withCard.getName());
        assertNotNull(withCard.getIdCard());
        assertEquals(Integer.valueOf(firstEmp), withCard.getIdCard().getEmpId());

        Emp withDept = mapper.findEmpWithDept(firstEmp);
        assertEquals(Integer.valueOf(deptId), withDept.getDeptInfo().getDeptno());
        assertEquals(withDept.getDepartment(), withDept.getDeptInfo().getDname());
        assertEquals("上海", withDept.getDeptInfo().getLoc());
        assertEquals(withDept.getDeptInfo().getDeptno(), mapper.findEmpWithDept(secondEmp).getDeptInfo().getDeptno());

        Dept department = mapper.findDeptWithEmps(deptId);
        assertEquals(Arrays.asList(firstEmp, secondEmp), department.getEmps().stream().map(Emp::getId).collect(Collectors.toList()));
        Project project = mapper.findProjectWithEmps(firstProject);
        assertEquals(Arrays.asList(firstEmp, secondEmp), project.getEmps().stream().map(Emp::getId).collect(Collectors.toList()));
        Emp employee = mapper.findEmpWithProjects(firstEmp);
        assertEquals(Arrays.asList(firstProject, secondProject), employee.getProjects().stream().map(Project::getProjectId).collect(Collectors.toList()));
    }

    @Test
    public void keepsParentsWithNoChildrenAndOmitsDeletedDepartment() throws SQLException {
        int unassigned = insert("INSERT INTO emp(emp_name) VALUES (?)", "无关联测试员工");
        int emptyDept = insert("INSERT INTO dept(dname, deleted) VALUES (?, 0)", "空测试部" + System.nanoTime());
        int emptyProject = insert("INSERT INTO project(project_name) VALUES (?)", "空测试项目" + System.nanoTime());
        assertNull(mapper.findEmpWithIdCard(unassigned).getIdCard());
        assertNull(mapper.findEmpWithDept(unassigned).getDeptInfo());
        assertTrue(mapper.findEmpWithProjects(unassigned).getProjects().isEmpty());
        assertTrue(mapper.findDeptWithEmps(emptyDept).getEmps().isEmpty());
        assertTrue(mapper.findProjectWithEmps(emptyProject).getEmps().isEmpty());

        execute("UPDATE dept SET deleted = 1 WHERE deptno = ?", deptId);
        assertNull(mapper.findEmpWithDept(firstEmp).getDeptInfo());
        assertNull(mapper.findDeptWithEmps(deptId));
    }

    @Test
    public void missingParentsReturnNull() {
        assertNull(mapper.findEmpWithIdCard(-1));
        assertNull(mapper.findEmpWithDept(-1));
        assertNull(mapper.findDeptWithEmps(-1));
        assertNull(mapper.findProjectWithEmps(-1));
        assertNull(mapper.findEmpWithProjects(-1));
    }

    @Test
    public void deletedEmployeeIsExcludedFromAllRelations() {
        cn.edu.course.mybatis.mapper.EmpMapper employees = session.getMapper(cn.edu.course.mybatis.mapper.EmpMapper.class);
        assertEquals(1, employees.removeRecord(firstEmp));
        assertNull(mapper.findEmpWithIdCard(firstEmp));
        assertNull(mapper.findEmpWithDept(firstEmp));
        assertNull(mapper.findEmpWithProjects(firstEmp));
        assertEquals(1, mapper.findDeptWithEmps(deptId).getEmps().size());
        assertEquals(Integer.valueOf(secondEmp), mapper.findProjectWithEmps(firstProject).getEmps().get(0).getId());
        assertEquals(1, employees.removeRecord(secondEmp));
        assertTrue(mapper.findDeptWithEmps(deptId).getEmps().isEmpty());
        assertTrue(mapper.findProjectWithEmps(firstProject).getEmps().isEmpty());
    }

    private int insert(String sql, Object... values) throws SQLException {
        try (PreparedStatement statement = session.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(statement, values);
            assertEquals(1, statement.executeUpdate());
            try (ResultSet keys = statement.getGeneratedKeys()) {
                assertTrue(keys.next());
                return keys.getInt(1);
            }
        }
    }

    private void execute(String sql, Object... values) throws SQLException {
        try (PreparedStatement statement = session.getConnection().prepareStatement(sql)) {
            bind(statement, values);
            statement.executeUpdate();
        }
    }

    private void bind(PreparedStatement statement, Object[] values) throws SQLException {
        for (int index = 0; index < values.length; index++) {
            statement.setObject(index + 1, values[index]);
        }
    }
}
