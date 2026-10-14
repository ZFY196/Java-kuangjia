package cn.edu.course.mybatis.test;

import cn.edu.course.mybatis.entity.Emp;
import cn.edu.course.mybatis.mapper.EmpMapper;
import cn.edu.course.mybatis.util.EmpQueryWrappers;
import cn.edu.course.mybatis.util.MyBatisUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.session.SqlSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class EmpMapperPlusTest {
    private SqlSession session;
    private EmpMapper mapper;
    private Emp first;
    private Emp second;
    private Emp departed;

    @Before
    public void setUp() {
        session = MyBatisUtil.openSession();
        mapper = session.getMapper(EmpMapper.class);
        first = employee("江澄月", "女", "智能运维部", "16480.00", 1);
        second = employee("邵云帆", "男", "智能运维部", "12530.00", 1);
        departed = employee("温栀宁", "女", "业务分析部", "19860.00", 0);
        for (Emp employee : Arrays.asList(first, second, departed)) {
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
    public void baseMapperCrudMapsFieldsAndAcceptsSerializableIds() {
        BaseMapper<Emp> baseMapper = mapper;
        Emp employee = employee("黎清和", "男", "数字服务部", "15270.00", 1);
        assertEquals(1, baseMapper.insert(employee));
        assertNotNull(employee.getId());
        Serializable id = employee.getId();
        Emp saved = baseMapper.selectById(id);
        assertNotNull(saved);
        assertEquals(employee.getName(), saved.getName());
        assertEquals(employee.getSex(), saved.getSex());
        assertEquals(employee.getDepartment(), saved.getDepartment());
        assertEquals(employee.getPosition(), saved.getPosition());
        assertEquals(employee.getMonthlyPay(), saved.getMonthlyPay());
        assertEquals(employee.getJoinedOn().getTime(), saved.getJoinedOn().getTime());
        assertEquals(employee.getActiveFlag(), saved.getActiveFlag());

        Emp patch = new Emp();
        patch.setId(employee.getId());
        patch.setDepartment("客户体验部");
        patch.setMonthlyPay(new BigDecimal("17190.00"));
        assertEquals(1, baseMapper.updateById(patch));
        Emp updated = baseMapper.selectById(id);
        assertEquals("客户体验部", updated.getDepartment());
        assertEquals(new BigDecimal("17190.00"), updated.getMonthlyPay());
        assertEquals(saved.getName(), updated.getName());
        assertEquals(saved.getSex(), updated.getSex());
        assertEquals(saved.getPosition(), updated.getPosition());
        assertEquals(saved.getJoinedOn(), updated.getJoinedOn());
        assertEquals(saved.getActiveFlag(), updated.getActiveFlag());

        assertEquals(1, baseMapper.deleteById(id));
        assertNull(baseMapper.selectById(id));
    }

    @Test
    public void queryAndLambdaWrappersCombineFiltersAndSortBySalary() {
        List<Integer> expected = Arrays.asList(first.getId(), second.getId());
        for (String blankName : Arrays.asList(null, "", "   ")) {
            assertEquals(expected, ids(mapper.selectList(
                    EmpQueryWrappers.byColumns(blankName, "", null).eq("dept", "智能运维部"))));
            assertEquals(expected, ids(mapper.selectList(
                    EmpQueryWrappers.byProperties(blankName, "", null).eq(Emp::getDepartment, "智能运维部"))));
        }

        assertEquals(Collections.singletonList(first.getId()), ids(mapper.selectList(
                EmpQueryWrappers.byColumns("澄", "女", new BigDecimal("15000.00")))));
        assertEquals(Collections.singletonList(first.getId()), ids(mapper.selectList(
                EmpQueryWrappers.byProperties("澄", "女", new BigDecimal("15000.00")))));
        assertTrue(mapper.selectList(EmpQueryWrappers.byColumns("澄", "男", null)).isEmpty());
        assertTrue(mapper.selectList(EmpQueryWrappers.byProperties("澄", "男", null)).isEmpty());
        assertTrue(mapper.selectList(EmpQueryWrappers.byColumns("澄", null, new BigDecimal("17000.00"))).isEmpty());
        assertTrue(mapper.selectList(EmpQueryWrappers.byProperties("澄", null, new BigDecimal("17000.00"))).isEmpty());

        // status=0 仍可按编号读取，只有明确的在职条件才将其排除。
        assertNotNull(mapper.selectById(departed.getId()));
        assertTrue(mapper.selectList(EmpQueryWrappers.byProperties("温栀宁", null, null)).isEmpty());
    }

    @Test
    public void customXmlUpdateAndBaseMapperWrapperUpdateCoexist() {
        Emp profile = mapper.selectById(first.getId());
        profile.setPosition("运维架构师");
        assertEquals(1, mapper.replaceProfile(profile));
        assertEquals("运维架构师", mapper.selectById(first.getId()).getPosition());

        LambdaUpdateWrapper<Emp> wrapper = new LambdaUpdateWrapper<Emp>()
                .eq(Emp::getId, first.getId())
                .set(Emp::getMonthlyPay, new BigDecimal("0.00"))
                .set(Emp::getActiveFlag, 0);
        assertEquals(1, mapper.update(null, wrapper));
        Emp updated = mapper.selectById(first.getId());
        assertEquals(new BigDecimal("0.00"), updated.getMonthlyPay());
        assertEquals(Integer.valueOf(0), updated.getActiveFlag());
        assertEquals("运维架构师", updated.getPosition());
        assertEquals(Collections.singletonList(first.getId()), ids(mapper.selectList(
                new LambdaQueryWrapper<Emp>().eq(Emp::getId, first.getId()).eq(Emp::getActiveFlag, 0))));
    }

    @Test
    public void paginationCountsAndReturnsDistinctPagesWithStableOrdering() {
        Emp third = employee("沈书珩", "男", "智能运维部", "14820.00", 1);
        Emp fourth = employee("程晚晴", "女", "智能运维部", "12530.00", 1);
        assertEquals(1, mapper.insert(third));
        assertEquals(1, mapper.insert(fourth));
        LambdaQueryWrapper<Emp> wrapper = EmpQueryWrappers.byProperties(null, null, null)
                .eq(Emp::getDepartment, "智能运维部");

        Page<Emp> firstPage = mapper.selectPage(new Page<>(1, 3), wrapper);
        assertEquals(4L, firstPage.getTotal());
        assertEquals(2L, firstPage.getPages());
        assertEquals(Arrays.asList(first.getId(), third.getId(), second.getId()),
                ids(firstPage.getRecords()));

        Page<Emp> lastPage = mapper.selectPage(new Page<>(2, 3), wrapper);
        assertEquals(4L, lastPage.getTotal());
        assertEquals(2L, lastPage.getPages());
        assertEquals(Collections.singletonList(fourth.getId()), ids(lastPage.getRecords()));

        Page<Emp> beyondLastPage = mapper.selectPage(new Page<>(3, 3), wrapper);
        assertEquals(4L, beyondLastPage.getTotal());
        assertTrue(beyondLastPage.getRecords().isEmpty());
    }

    @Test
    public void paginationReturnsZeroCountAndNoRecordsWhenNothingMatches() {
        Page<Emp> page = mapper.selectPage(new Page<>(1, 3),
                EmpQueryWrappers.byColumns("不存在的分页样例", null, null));
        assertEquals(0L, page.getTotal());
        assertEquals(0L, page.getPages());
        assertTrue(page.getRecords().isEmpty());
    }

    private static Emp employee(String name, String gender, String department, String salary, int status) {
        Emp employee = new Emp();
        employee.setName(name);
        employee.setSex(gender);
        employee.setDepartment(department);
        employee.setPosition("平台开发工程师");
        employee.setMonthlyPay(new BigDecimal(salary));
        employee.setJoinedOn(Date.valueOf("2026-07-09"));
        employee.setActiveFlag(status);
        return employee;
    }

    private static List<Integer> ids(List<Emp> employees) {
        return employees.stream().map(Emp::getId).collect(Collectors.toList());
    }
}
