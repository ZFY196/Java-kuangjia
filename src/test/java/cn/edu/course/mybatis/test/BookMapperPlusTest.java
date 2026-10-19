package cn.edu.course.mybatis.test;

import cn.edu.course.mybatis.entity.Book;
import cn.edu.course.mybatis.mapper.BookMapper;
import cn.edu.course.mybatis.util.BookQueryWrappers;
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

public class BookMapperPlusTest {
    private SqlSession session;
    private BookMapper mapper;
    private Book first;
    private Book second;
    private Book offSale;

    @Before
    public void setUp() {
        session = MyBatisUtil.openSession();
        mapper = session.getMapper(BookMapper.class);
        first = book("深入理解MyBatis", "精", "计算机实验出版社", "164.80", 1);
        second = book("Java并发编程", "平", "计算机实验出版社", "125.30", 1);
        offSale = book("软件测试基础", "精", "管理实验出版社", "198.60", 0);
        for (Book book : Arrays.asList(first, second, offSale)) {
            assertEquals(1, mapper.insert(book));
            assertNotNull(book.getId());
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
        BaseMapper<Book> baseMapper = mapper;
        Book book = book("Web应用开发", "平", "数字实验出版社", "152.70", 1);
        assertEquals(1, baseMapper.insert(book));
        assertNotNull(book.getId());
        Serializable id = book.getId();
        Book saved = baseMapper.selectById(id);
        assertNotNull(saved);
        assertEquals(book.getTitle(), saved.getTitle());
        assertEquals(book.getBinding(), saved.getBinding());
        assertEquals(book.getPublisher(), saved.getPublisher());
        assertEquals(book.getCategory(), saved.getCategory());
        assertEquals(book.getPrice(), saved.getPrice());
        assertEquals(book.getPublishedOn().getTime(), saved.getPublishedOn().getTime());
        assertEquals(book.getSaleStatus(), saved.getSaleStatus());

        Book patch = new Book();
        patch.setId(book.getId());
        patch.setPublisher("应用实验出版社");
        patch.setPrice(new BigDecimal("171.90"));
        assertEquals(1, baseMapper.updateById(patch));
        Book updated = baseMapper.selectById(id);
        assertEquals("应用实验出版社", updated.getPublisher());
        assertEquals(new BigDecimal("171.90"), updated.getPrice());
        assertEquals(saved.getTitle(), updated.getTitle());
        assertEquals(saved.getBinding(), updated.getBinding());
        assertEquals(saved.getCategory(), updated.getCategory());
        assertEquals(saved.getPublishedOn(), updated.getPublishedOn());
        assertEquals(saved.getSaleStatus(), updated.getSaleStatus());

        assertEquals(1, baseMapper.deleteById(id));
        assertNull(baseMapper.selectById(id));
    }

    @Test
    public void queryAndLambdaWrappersCombineFiltersAndSortByPrice() {
        List<Integer> expected = Arrays.asList(first.getId(), second.getId());
        for (String blankName : Arrays.asList(null, "", "   ")) {
            assertEquals(expected, ids(mapper.selectList(
                    BookQueryWrappers.byColumns(blankName, "", null).eq("publisher", "计算机实验出版社"))));
            assertEquals(expected, ids(mapper.selectList(
                    BookQueryWrappers.byProperties(blankName, "", null).eq(Book::getPublisher, "计算机实验出版社"))));
        }

        assertEquals(Collections.singletonList(first.getId()), ids(mapper.selectList(
                BookQueryWrappers.byColumns("深入", "精", new BigDecimal("150.00")))));
        assertEquals(Collections.singletonList(first.getId()), ids(mapper.selectList(
                BookQueryWrappers.byProperties("深入", "精", new BigDecimal("150.00")))));
        assertTrue(mapper.selectList(BookQueryWrappers.byColumns("深入", "平", null)).isEmpty());
        assertTrue(mapper.selectList(BookQueryWrappers.byProperties("深入", "平", null)).isEmpty());
        assertTrue(mapper.selectList(BookQueryWrappers.byColumns("深入", null, new BigDecimal("170.00"))).isEmpty());
        assertTrue(mapper.selectList(BookQueryWrappers.byProperties("深入", null, new BigDecimal("170.00"))).isEmpty());

        // status=0 仍可按编号读取，只有明确的在售条件才将其排除。
        assertNotNull(mapper.selectById(offSale.getId()));
        assertTrue(mapper.selectList(BookQueryWrappers.byProperties("软件测试基础", null, null)).isEmpty());
    }

    @Test
    public void customXmlUpdateAndBaseMapperWrapperUpdateCoexist() {
        Book details = mapper.selectById(first.getId());
        details.setCategory("软件架构");
        assertEquals(1, mapper.replaceDetails(details));
        assertEquals("软件架构", mapper.selectById(first.getId()).getCategory());

        LambdaUpdateWrapper<Book> wrapper = new LambdaUpdateWrapper<Book>()
                .eq(Book::getId, first.getId())
                .set(Book::getPrice, new BigDecimal("0.00"))
                .set(Book::getSaleStatus, 0);
        assertEquals(1, mapper.update(null, wrapper));
        Book updated = mapper.selectById(first.getId());
        assertEquals(new BigDecimal("0.00"), updated.getPrice());
        assertEquals(Integer.valueOf(0), updated.getSaleStatus());
        assertEquals("软件架构", updated.getCategory());
        assertEquals(Collections.singletonList(first.getId()), ids(mapper.selectList(
                new LambdaQueryWrapper<Book>().eq(Book::getId, first.getId()).eq(Book::getSaleStatus, 0))));
    }

    @Test
    public void paginationCountsAndReturnsDistinctPagesWithStableOrdering() {
        Book third = book("网络编程实践", "平", "计算机实验出版社", "148.20", 1);
        Book fourth = book("数据结构练习", "精", "计算机实验出版社", "125.30", 1);
        assertEquals(1, mapper.insert(third));
        assertEquals(1, mapper.insert(fourth));
        LambdaQueryWrapper<Book> wrapper = BookQueryWrappers.byProperties(null, null, null)
                .eq(Book::getPublisher, "计算机实验出版社");

        Page<Book> firstPage = mapper.selectPage(new Page<>(1, 3), wrapper);
        assertEquals(4L, firstPage.getTotal());
        assertEquals(2L, firstPage.getPages());
        assertEquals(Arrays.asList(first.getId(), third.getId(), second.getId()),
                ids(firstPage.getRecords()));

        Page<Book> lastPage = mapper.selectPage(new Page<>(2, 3), wrapper);
        assertEquals(4L, lastPage.getTotal());
        assertEquals(2L, lastPage.getPages());
        assertEquals(Collections.singletonList(fourth.getId()), ids(lastPage.getRecords()));

        Page<Book> beyondLastPage = mapper.selectPage(new Page<>(3, 3), wrapper);
        assertEquals(4L, beyondLastPage.getTotal());
        assertTrue(beyondLastPage.getRecords().isEmpty());
    }

    @Test
    public void paginationReturnsZeroCountAndNoRecordsWhenNothingMatches() {
        Page<Book> page = mapper.selectPage(new Page<>(1, 3),
                BookQueryWrappers.byColumns("不存在的分页样例", null, null));
        assertEquals(0L, page.getTotal());
        assertEquals(0L, page.getPages());
        assertTrue(page.getRecords().isEmpty());
    }

    private static Book book(String title, String binding, String publisher, String price, int status) {
        Book book = new Book();
        book.setTitle(title);
        book.setBinding(binding);
        book.setPublisher(publisher);
        book.setCategory("编程技术");
        book.setPrice(new BigDecimal(price));
        book.setPublishedOn(Date.valueOf("2026-07-09"));
        book.setSaleStatus(status);
        return book;
    }

    private static List<Integer> ids(List<Book> books) {
        return books.stream().map(Book::getId).collect(Collectors.toList());
    }
}
