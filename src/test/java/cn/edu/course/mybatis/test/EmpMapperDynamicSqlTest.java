package cn.edu.course.mybatis.test;

import cn.edu.course.mybatis.entity.Emp;
import cn.edu.course.mybatis.entity.StaffSearch;
import cn.edu.course.mybatis.mapper.EmpMapper;
import cn.edu.course.mybatis.util.MyBatisUtil;
import org.apache.ibatis.session.SqlSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class EmpMapperDynamicSqlTest {
    private SqlSession session;
    private EmpMapper mapper;
    private Emp activeEmployee;
    private Emp departedEmployee;
    private Emp platformEmployee;

    @Before
    public void setUp() {
        session = MyBatisUtil.openSession();
        mapper = session.getMapper(EmpMapper.class);
        activeEmployee = employee("顾若宁", "女", "算法验证部", "算法测试工程师", "12640.00", 1);
        departedEmployee = employee("唐若溪", "女", "算法验证部", "数据质量专员", "9720.00", 0);
        platformEmployee = employee("陆星舟", "男", "平台运维部", "运维工程师", "13750.00", 1);
        for (Emp employee : Arrays.asList(activeEmployee, departedEmployee, platformEmployee)) {
            assertEquals(1, mapper.insert(employee));
            assertNotNull(employee.getId());
        }
    }

    @After
    public void tearDown() {
        if (session != null) {
            try {
                session.rollback();
            } finally {
                session.close();
            }
        }
    }

    @Test
    public void conditionQueryCombinesNameGenderDepartmentAndStatus() {
        StaffSearch filter = new StaffSearch();
        filter.setNameText("若");
        assertEquals(Arrays.asList(activeEmployee.getId(), departedEmployee.getId()),
                ids(mapper.searchRoster(filter)));

        filter.setSex("女");
        filter.setDepartment("算法验证部");
        filter.setActiveFlag(1);
        assertEquals(Collections.singletonList(activeEmployee.getId()), ids(mapper.searchRoster(filter)));
        filter.setActiveFlag(0);
        assertEquals(Collections.singletonList(departedEmployee.getId()), ids(mapper.searchRoster(filter)));
        filter.setSex("男");
        assertTrue(mapper.searchRoster(filter).isEmpty());

        StaffSearch departmentOnly = new StaffSearch();
        departmentOnly.setDepartment("平台运维部");
        assertEquals(Collections.singletonList(platformEmployee.getId()),
                ids(mapper.searchRoster(departmentOnly)));
        departmentOnly.setSex("女");
        assertTrue(mapper.searchRoster(departmentOnly).isEmpty());
    }

    @Test
    public void trimAndWhereReturnTheSameRowsIncludingEmptyConditions() {
        StaffSearch blank = new StaffSearch();
        blank.setNameText("");
        blank.setSex("");
        blank.setDepartment("");
        List<Integer> allIds = ids(mapper.listRoster());
        assertEquals(allIds, ids(mapper.searchRoster(null)));
        assertEquals(allIds, ids(mapper.searchRoster(blank)));

        StaffSearch nameOnly = new StaffSearch();
        nameOnly.setNameText("若");
        StaffSearch genderOnly = new StaffSearch();
        genderOnly.setSex("男");
        StaffSearch departmentOnly = new StaffSearch();
        departmentOnly.setDepartment("算法验证部");
        StaffSearch statusOnly = new StaffSearch();
        statusOnly.setActiveFlag(0);
        StaffSearch combined = new StaffSearch();
        combined.setNameText("若");
        combined.setSex("女");
        combined.setDepartment("算法验证部");
        combined.setActiveFlag(1);

        for (StaffSearch filter : Arrays.asList(null, blank, nameOnly, genderOnly, departmentOnly, statusOnly, combined)) {
            assertEquals(ids(mapper.searchRoster(filter)), ids(mapper.searchRosterWithTrim(filter)));
        }
    }

    @Test
    public void dynamicUpdateChangesOnlyProvidedFieldsAndAcceptsZeroValues() {
        Emp original = mapper.selectById(activeEmployee.getId());
        Emp patch = new Emp();
        patch.setId(original.getId());
        patch.setMonthlyPay(new BigDecimal("0.00"));
        patch.setActiveFlag(0);
        assertEquals(1, mapper.patchProfile(patch));

        Emp updated = mapper.selectById(original.getId());
        assertEquals(0, updated.getMonthlyPay().compareTo(BigDecimal.ZERO));
        assertEquals(Integer.valueOf(0), updated.getActiveFlag());
        assertEquals(original.getName(), updated.getName());
        assertEquals(original.getSex(), updated.getSex());
        assertEquals(original.getDepartment(), updated.getDepartment());
        assertEquals(original.getPosition(), updated.getPosition());
        assertEquals(original.getJoinedOn(), updated.getJoinedOn());

        Emp profilePatch = new Emp();
        profilePatch.setId(original.getId());
        profilePatch.setName("顾以宁");
        profilePatch.setDepartment("数据治理部");
        profilePatch.setPosition("数据分析工程师");
        assertEquals(1, mapper.patchProfile(profilePatch));
        Emp profile = mapper.selectById(original.getId());
        assertEquals("顾以宁", profile.getName());
        assertEquals("数据治理部", profile.getDepartment());
        assertEquals("数据分析工程师", profile.getPosition());
        assertEquals(0, profile.getMonthlyPay().compareTo(BigDecimal.ZERO));
        assertEquals(Integer.valueOf(0), profile.getActiveFlag());
        assertEquals(original.getSex(), profile.getSex());
        assertEquals(original.getJoinedOn(), profile.getJoinedOn());
    }

    @Test
    public void dynamicUpdateWithoutFieldsOrIdDoesNotChangeRows() {
        List<String> before = snapshots(mapper.listRoster());
        Emp noFields = new Emp();
        noFields.setId(activeEmployee.getId());
        assertEquals(0, mapper.patchProfile(noFields));
        Emp noId = new Emp();
        noId.setMonthlyPay(new BigDecimal("100.00"));
        assertEquals(0, mapper.patchProfile(noId));
        assertEquals(0, mapper.patchProfile(null));
        assertEquals(before, snapshots(mapper.listRoster()));
    }

    @Test
    public void batchInsertReturnsKeysAndBatchDeleteRemovesOnlyRequestedRows() {
        List<String> before = snapshots(mapper.listRoster());
        Emp first = employee("许清越", "男", "数据集成部", "集成开发工程师", "14230.00", 1);
        Emp second = employee("乔雨桐", "女", "数据集成部", "数据协调专员", "10890.00", 0);
        assertEquals(2, mapper.addRoster(Arrays.asList(first, second)));
        assertNotNull(first.getId());
        assertNotNull(second.getId());
        assertNotEquals(first.getId(), second.getId());
        assertEquals("许清越", mapper.selectById(first.getId()).getName());
        Emp savedSecond = mapper.selectById(second.getId());
        assertEquals("乔雨桐", savedSecond.getName());
        assertEquals(second.getMonthlyPay(), savedSecond.getMonthlyPay());
        assertEquals(second.getActiveFlag(), savedSecond.getActiveFlag());

        assertEquals(2, mapper.removeRoster(Arrays.asList(first.getId(), second.getId())));
        assertNull(mapper.selectById(first.getId()));
        assertNull(mapper.selectById(second.getId()));
        assertEquals(before, snapshots(mapper.listRoster()));
    }

    @Test
    public void emptyBatchOperationsDoNotChangeRows() {
        List<String> before = snapshots(mapper.listRoster());
        assertEquals(0, mapper.removeRoster(Collections.emptyList()));
        assertEquals(0, mapper.removeRoster(null));
        assertEquals(0, mapper.addRoster(Collections.emptyList()));
        assertEquals(0, mapper.addRoster(null));
        assertEquals(before, snapshots(mapper.listRoster()));
    }

    @Test
    public void normalizedSearchCriteriaAgreeAcrossXmlAndPlus() {
        StaffSearch request = new StaffSearch().setNameText("  若  ")
                .setSex(" 女 ").setDepartment(" 算法验证部 ")
                .setMinimumPay(new BigDecimal("10000.00"));
        List<Integer> expected = Collections.singletonList(activeEmployee.getId());
        assertEquals(expected, ids(mapper.searchRoster(request)));
        assertEquals(expected, ids(mapper.searchRosterWithTrim(request)));
        assertEquals(expected, ids(mapper.selectList(
                cn.edu.course.mybatis.util.EmpQueryWrappers.byColumns(request))));
        assertEquals(expected, ids(mapper.selectList(
                cn.edu.course.mybatis.util.EmpQueryWrappers.byProperties(request))));

        request.setNameText("  ").setSex(" ").setDepartment(null).setMinimumPay(null);
        assertEquals(ids(mapper.listRoster()), ids(mapper.searchRoster(request)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void searchRejectsNegativeMinimumSalary() {
        new StaffSearch().setMinimumPay(new BigDecimal("-1.00"));
    }

    @Test
    public void choosePrioritizesExactNameThenDepartmentThenAllRows() {
        StaffSearch query = new StaffSearch();
        query.setNameText("顾若宁");
        query.setDepartment("平台运维部");
        assertEquals(Collections.singletonList(activeEmployee.getId()), ids(mapper.findPreferred(query)));

        query.setNameText("若");
        assertTrue(mapper.findPreferred(query).isEmpty());
        assertTrue(mapper.searchRoster(query).isEmpty());
        query.setNameText("顾若宁");
        assertTrue(mapper.searchRoster(query).isEmpty());

        query.setNameText("");
        query.setDepartment("算法验证部");
        query.setSex("男");
        query.setActiveFlag(0);
        assertEquals(Arrays.asList(activeEmployee.getId(), departedEmployee.getId()),
                ids(mapper.findPreferred(query)));

        query.setDepartment("");
        assertEquals(ids(mapper.listRoster()), ids(mapper.findPreferred(query)));
        assertEquals(ids(mapper.listRoster()), ids(mapper.findPreferred(null)));
    }

    private static Emp employee(String name, String gender, String department, String post,
                                String salary, int status) {
        Emp employee = new Emp();
        employee.setName(name);
        employee.setSex(gender);
        employee.setDepartment(department);
        employee.setPosition(post);
        employee.setMonthlyPay(new BigDecimal(salary));
        employee.setJoinedOn(Date.valueOf("2026-05-18"));
        employee.setActiveFlag(status);
        return employee;
    }

    private static List<Integer> ids(List<Emp> employees) {
        return employees.stream().map(Emp::getId).collect(Collectors.toList());
    }

    private static List<String> snapshots(List<Emp> employees) {
        return employees.stream().map(Emp::toString).collect(Collectors.toList());
    }
}
