package cn.edu.course.mybatis.test;

import cn.edu.course.mybatis.entity.Emp;
import cn.edu.course.mybatis.entity.StaffSearch;
import cn.edu.course.mybatis.mapper.EmpMapper;
import cn.edu.course.mybatis.util.MyBatisUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.session.SqlSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/** 七项补充功能全部使用现有员工字段和 Mapper，每个测试结束后回滚。 */
public class EmpMapperAdvancedTest {
    private SqlSession session;
    private EmpMapper mapper;
    private Emp first;
    private Emp second;
    private Emp departed;
    private String namePrefix;
    private String department;

    @Before
    public void setUp() {
        session = MyBatisUtil.openSession();
        mapper = session.getMapper(EmpMapper.class);
        String suffix = Long.toString(System.nanoTime());
        namePrefix = "进阶员工" + suffix;
        department = "进阶研发部" + suffix;
        first = employee(namePrefix + "甲", department, "16000.00", 1);
        second = employee(namePrefix + "乙", department, "12000.00", 1);
        departed = employee(namePrefix + "丙", "进阶服务部" + suffix, "8000.00", 0);
        for (Emp employee : Arrays.asList(first, second, departed)) {
            assertEquals(1, mapper.insert(employee));
            assertNotNull(employee.getId());
        }
    }

    @After
    public void tearDown() {
        if (session != null) {
            try {
                session.rollback(true);
            } finally {
                session.close();
            }
        }
    }

    @Test
    public void autoFillCreatesAndRefreshesEmployeeTimes() {
        heading("自动填充：员工创建时间与更新时间");
        assertNotNull(first.getCreateTime());
        assertNotNull(first.getUpdateTime());
        Emp loaded = mapper.selectById(first.getId());
        LocalDateTime created = loaded.getCreateTime();
        LocalDateTime oldTime = LocalDateTime.of(2000, 1, 1, 0, 0);
        loaded.setCreateTime(oldTime);
        loaded.setUpdateTime(oldTime);
        loaded.setPosition("高级开发工程师");
        assertEquals(1, mapper.updateById(loaded));
        Emp updated = mapper.findRecord(first.getId());
        assertEquals(created, updated.getCreateTime());
        assertTrue(updated.getUpdateTime().isAfter(oldTime));
        assertFalse(updated.getUpdateTime().isBefore(created));
        System.out.println("创建时间: " + updated.getCreateTime() + "，更新时间: " + updated.getUpdateTime());

        Emp classic = employee(namePrefix + "原生", department, "9000.00", 1);
        assertEquals(1, mapper.createRecord(classic));
        Emp classicSaved = mapper.findRecord(classic.getId());
        assertNotNull(classicSaved.getCreateTime());
        assertNotNull(classicSaved.getUpdateTime());
    }

    @Test
    public void logicDeleteIsSeparateFromDepartureAndXmlAlsoFiltersDeletedRows() throws Exception {
        heading("逻辑删除：与在职/离职状态独立");
        assertNotNull(mapper.selectById(departed.getId()));
        Emp stale = mapper.selectById(first.getId());
        assertEquals(1, mapper.deleteById(first.getId()));
        assertNull(mapper.selectById(first.getId()));
        assertNull(mapper.findRecord(first.getId()));
        assertTrue(mapper.listRoster().stream().noneMatch(employee -> first.getId().equals(employee.getId())));
        StaffSearch deletedOnly = new StaffSearch().setNameText(first.getName());
        assertTrue(mapper.searchRoster(deletedOnly).isEmpty());
        assertTrue(mapper.searchRosterWithTrim(deletedOnly).isEmpty());
        assertTrue(mapper.findPreferred(deletedOnly).isEmpty());
        stale.setPosition("已删除记录不应被更新");
        assertEquals(0, mapper.updateById(stale));
        assertEquals(0, mapper.replaceProfile(stale));
        assertEquals(0, mapper.patchProfile(stale));
        assertEquals(0, mapper.removeRecord(first.getId()));

        try (PreparedStatement statement = session.getConnection()
                .prepareStatement("SELECT status, deleted FROM emp WHERE emp_id = ?")) {
            statement.setInt(1, first.getId());
            try (ResultSet rows = statement.executeQuery()) {
                assertTrue(rows.next());
                assertEquals(1, rows.getInt("status"));
                assertEquals(1, rows.getInt("deleted"));
            }
        }
        assertEquals(1, mapper.removeRecord(second.getId()));
        assertNull(mapper.selectById(second.getId()));
        assertEquals(1, mapper.removeRoster(Arrays.asList(first.getId(), departed.getId())));
        assertNull(mapper.selectById(departed.getId()));
        System.out.println("离职员工原本可查询；删除后物理行仍在，MP、XML及批量查询均排除已删除员工");
    }

    @Test
    public void optimisticLockRejectsStaleSalaryAndXmlUpdateInvalidatesOldSnapshots() {
        heading("乐观锁：防止旧版本覆盖员工薪资");
        Emp current = mapper.selectById(first.getId());
        session.clearCache();
        Emp stale = mapper.selectById(first.getId());
        assertNotSame(current, stale);
        assertEquals(Integer.valueOf(0), current.getVersion());
        current.setMonthlyPay(new BigDecimal("18000.00"));
        assertEquals(1, mapper.updateById(current));
        assertEquals(Integer.valueOf(1), mapper.selectById(first.getId()).getVersion());
        stale.setMonthlyPay(new BigDecimal("100.00"));
        assertEquals(0, mapper.updateById(stale));
        assertEquals(new BigDecimal("18000.00"), mapper.selectById(first.getId()).getMonthlyPay());

        Emp xmlSnapshot = mapper.findRecord(second.getId());
        session.clearCache();
        Emp mpSnapshot = mapper.selectById(second.getId());
        xmlSnapshot.setPosition("XML更新后的岗位");
        assertEquals(1, mapper.replaceProfile(xmlSnapshot));
        assertEquals(0, mapper.replaceProfile(mpSnapshot));
        assertEquals(0, mapper.patchProfile(mpSnapshot));
        assertEquals(0, mapper.updateById(mpSnapshot));
        assertEquals("XML更新后的岗位", mapper.selectById(second.getId()).getPosition());
        System.out.println("version: 0 → 1；旧版本更新影响行数: 0；XML更新也会推进版本号");
    }

    @Test
    public void notNullFieldStrategyRetainsPositionAndSupportsExplicitClear() {
        heading("字段策略：岗位null保留、空串更新、显式清空");
        Emp patch = new Emp();
        patch.setId(first.getId());
        patch.setPosition(null);
        patch.setMonthlyPay(BigDecimal.ZERO);
        assertEquals(1, mapper.updateById(patch));
        Emp saved = mapper.selectById(first.getId());
        assertEquals("开发工程师", saved.getPosition());
        assertEquals(0, BigDecimal.ZERO.compareTo(saved.getMonthlyPay()));
        patch.setPosition("");
        assertEquals(1, mapper.updateById(patch));
        assertEquals("", mapper.selectById(first.getId()).getPosition());
        assertEquals(1, mapper.update(new Emp(), new LambdaUpdateWrapper<Emp>()
                .eq(Emp::getId, first.getId()).set(Emp::getPosition, null)));
        assertNull(mapper.selectById(first.getId()).getPosition());
        System.out.println("岗位 NOT_NULL 与零薪资、空字符串、set(null) 验证通过");
    }

    @Test
    public void partialFieldsAndMapsReturnEmployeeNameDepartmentAndSalary() {
        heading("部分字段查询：员工简表与Map结果");
        List<Emp> partial = mapper.selectBriefRoster(new StaffSearch().setDepartment(department));
        assertEquals(2, partial.size());
        assertEquals(first.getId(), partial.get(0).getId());
        assertEquals(first.getName(), partial.get(0).getName());
        assertEquals(department, partial.get(0).getDepartment());
        assertEquals(first.getMonthlyPay(), partial.get(0).getMonthlyPay());
        assertNull(partial.get(0).getSex());
        assertNull(partial.get(0).getPosition());
        assertNull(partial.get(0).getCreateTime());
        List<Map<String, Object>> maps = mapper.selectMaps(scope()
                .select("emp_name", "dept", "salary").orderByAsc("emp_id"));
        assertEquals(3, maps.size());
        assertEquals(3, maps.get(0).size());
        assertEquals(first.getName(), maps.get(0).get("emp_name"));
        maps.forEach(System.out::println);
    }

    @Test
    public void departmentGroupingCountsEmployeesAndAveragesSalary() {
        heading("分组聚合：部门人数与平均薪资");
        Map<String, Object> group = mapper.summarizeDepartments().stream()
                .filter(row -> department.equals(row.get("dept"))).findFirst().orElseThrow(AssertionError::new);
        assertEquals(2L, ((Number) group.get("employee_count")).longValue());
        assertEquals(0, new BigDecimal("14000.00").compareTo((BigDecimal) group.get("average_salary")));
        System.out.println(group);
        assertEquals(1, mapper.deleteById(second.getId()));
        Map<String, Object> remaining = mapper.summarizeDepartments().stream()
                .filter(row -> department.equals(row.get("dept"))).findFirst().orElseThrow(AssertionError::new);
        assertEquals(1L, ((Number) remaining.get("employee_count")).longValue());
        assertEquals(0, first.getMonthlyPay().compareTo((BigDecimal) remaining.get("average_salary")));
    }

    @Test
    public void mapEqualityUsesEmployeeColumnsAndFiltersDeletedRows() {
        heading("Map等值查询：员工姓名与部门");
        Map<String, Object> conditions = new HashMap<>();
        conditions.put("emp_name", first.getName());
        conditions.put("dept", department);
        conditions.put("status", 1);
        List<Emp> found = mapper.selectByMap(conditions);
        assertEquals(1, found.size());
        assertEquals(first.getId(), found.get(0).getId());
        System.out.println(found.get(0));
        conditions.put("status", 0);
        assertTrue(mapper.selectByMap(conditions).isEmpty());
        conditions.put("status", 1);
        assertEquals(1, mapper.deleteById(first.getId()));
        assertTrue(mapper.selectByMap(conditions).isEmpty());
    }

    private QueryWrapper<Emp> scope() {
        return new QueryWrapper<Emp>().likeRight("emp_name", namePrefix);
    }

    private static Emp employee(String name, String department, String salary, int status) {
        Emp employee = new Emp();
        employee.setName(name);
        employee.setSex("女");
        employee.setDepartment(department);
        employee.setPosition("开发工程师");
        employee.setMonthlyPay(new BigDecimal(salary));
        employee.setJoinedOn(java.sql.Date.valueOf("2026-10-15"));
        employee.setActiveFlag(status);
        return employee;
    }

    private static void heading(String title) {
        System.out.println("\n========== " + title + " ==========");
    }
}
