package cn.edu.course.mybatis.test;

import cn.edu.course.mybatis.entity.Book;
import cn.edu.course.mybatis.entity.BookSearch;
import cn.edu.course.mybatis.mapper.BookMapper;
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

/** 七项补充功能全部使用现有图书字段和 Mapper，每个测试结束后回滚。 */
public class BookMapperAdvancedTest {
    private SqlSession session;
    private BookMapper mapper;
    private Book first;
    private Book second;
    private Book offSale;
    private String titlePrefix;
    private String publisher;

    @Before
    public void setUp() {
        session = MyBatisUtil.openSession();
        mapper = session.getMapper(BookMapper.class);
        String suffix = Long.toString(System.nanoTime());
        titlePrefix = "进阶图书" + suffix;
        publisher = "进阶技术出版社" + suffix;
        first = book(titlePrefix + "甲", publisher, "160.00", 1);
        second = book(titlePrefix + "乙", publisher, "120.00", 1);
        offSale = book(titlePrefix + "丙", "进阶综合出版社" + suffix, "80.00", 0);
        for (Book book : Arrays.asList(first, second, offSale)) {
            assertEquals(1, mapper.insert(book));
            assertNotNull(book.getId());
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
    public void autoFillCreatesAndRefreshesBookTimes() {
        heading("自动填充：图书创建时间与更新时间");
        assertNotNull(first.getCreateTime());
        assertNotNull(first.getUpdateTime());
        Book loaded = mapper.selectById(first.getId());
        LocalDateTime created = loaded.getCreateTime();
        LocalDateTime oldTime = LocalDateTime.of(2000, 1, 1, 0, 0);
        loaded.setCreateTime(oldTime);
        loaded.setUpdateTime(oldTime);
        loaded.setCategory("高级编程");
        assertEquals(1, mapper.updateById(loaded));
        Book updated = mapper.findRecord(first.getId());
        assertEquals(created, updated.getCreateTime());
        assertTrue(updated.getUpdateTime().isAfter(oldTime));
        assertFalse(updated.getUpdateTime().isBefore(created));
        System.out.println("创建时间: " + updated.getCreateTime() + "，更新时间: " + updated.getUpdateTime());

        Book classic = book(titlePrefix + "原生", publisher, "90.00", 1);
        assertEquals(1, mapper.createRecord(classic));
        Book classicSaved = mapper.findRecord(classic.getId());
        assertNotNull(classicSaved.getCreateTime());
        assertNotNull(classicSaved.getUpdateTime());
    }

    @Test
    public void logicDeleteIsSeparateFromOffSaleAndXmlAlsoFiltersDeletedRows() throws Exception {
        heading("逻辑删除：与在售/下架状态独立");
        assertNotNull(mapper.selectById(offSale.getId()));
        Book stale = mapper.selectById(first.getId());
        assertEquals(1, mapper.deleteById(first.getId()));
        assertNull(mapper.selectById(first.getId()));
        assertNull(mapper.findRecord(first.getId()));
        assertTrue(mapper.listBooks().stream().noneMatch(book -> first.getId().equals(book.getId())));
        BookSearch deletedOnly = new BookSearch().setTitleText(first.getTitle());
        assertTrue(mapper.searchBooks(deletedOnly).isEmpty());
        assertTrue(mapper.searchBooksWithTrim(deletedOnly).isEmpty());
        assertTrue(mapper.findPreferred(deletedOnly).isEmpty());
        stale.setCategory("已删除记录不应被更新");
        assertEquals(0, mapper.updateById(stale));
        assertEquals(0, mapper.replaceDetails(stale));
        assertEquals(0, mapper.patchDetails(stale));
        assertEquals(0, mapper.removeRecord(first.getId()));

        try (PreparedStatement statement = session.getConnection()
                .prepareStatement("SELECT status, deleted FROM book WHERE book_id = ?")) {
            statement.setInt(1, first.getId());
            try (ResultSet rows = statement.executeQuery()) {
                assertTrue(rows.next());
                assertEquals(1, rows.getInt("status"));
                assertEquals(1, rows.getInt("deleted"));
            }
        }
        assertEquals(1, mapper.removeRecord(second.getId()));
        assertNull(mapper.selectById(second.getId()));
        assertEquals(1, mapper.removeBooks(Arrays.asList(first.getId(), offSale.getId())));
        assertNull(mapper.selectById(offSale.getId()));
        System.out.println("下架图书原本可查询；删除后物理行仍在，MP、XML及批量查询均排除已删除图书");
    }

    @Test
    public void optimisticLockRejectsStalePriceAndXmlUpdateInvalidatesOldSnapshots() {
        heading("乐观锁：防止旧版本覆盖图书定价");
        Book current = mapper.selectById(first.getId());
        session.clearCache();
        Book stale = mapper.selectById(first.getId());
        assertNotSame(current, stale);
        assertEquals(Integer.valueOf(0), current.getVersion());
        current.setPrice(new BigDecimal("180.00"));
        assertEquals(1, mapper.updateById(current));
        assertEquals(Integer.valueOf(1), mapper.selectById(first.getId()).getVersion());
        stale.setPrice(new BigDecimal("1.00"));
        assertEquals(0, mapper.updateById(stale));
        assertEquals(new BigDecimal("180.00"), mapper.selectById(first.getId()).getPrice());

        Book xmlSnapshot = mapper.findRecord(second.getId());
        session.clearCache();
        Book mpSnapshot = mapper.selectById(second.getId());
        xmlSnapshot.setCategory("XML更新后的分类");
        assertEquals(1, mapper.replaceDetails(xmlSnapshot));
        assertEquals(0, mapper.replaceDetails(mpSnapshot));
        assertEquals(0, mapper.patchDetails(mpSnapshot));
        assertEquals(0, mapper.updateById(mpSnapshot));
        assertEquals("XML更新后的分类", mapper.selectById(second.getId()).getCategory());
        System.out.println("version: 0 → 1；旧版本更新影响行数: 0；XML更新也会推进版本号");
    }

    @Test
    public void notNullFieldStrategyRetainsCategoryAndSupportsExplicitClear() {
        heading("字段策略：分类null保留、空串更新、显式清空");
        Book patch = new Book();
        patch.setId(first.getId());
        patch.setCategory(null);
        patch.setPrice(BigDecimal.ZERO);
        assertEquals(1, mapper.updateById(patch));
        Book saved = mapper.selectById(first.getId());
        assertEquals("编程技术", saved.getCategory());
        assertEquals(0, BigDecimal.ZERO.compareTo(saved.getPrice()));
        patch.setCategory("");
        assertEquals(1, mapper.updateById(patch));
        assertEquals("", mapper.selectById(first.getId()).getCategory());
        assertEquals(1, mapper.update(new Book(), new LambdaUpdateWrapper<Book>()
                .eq(Book::getId, first.getId()).set(Book::getCategory, null)));
        assertNull(mapper.selectById(first.getId()).getCategory());
        System.out.println("分类 NOT_NULL 与零定价、空字符串、set(null) 验证通过");
    }

    @Test
    public void partialFieldsAndMapsReturnBookNamePublisherAndPrice() {
        heading("部分字段查询：图书简表与Map结果");
        List<Book> partial = mapper.selectBriefBooks(new BookSearch().setPublisher(publisher));
        assertEquals(2, partial.size());
        assertEquals(first.getId(), partial.get(0).getId());
        assertEquals(first.getTitle(), partial.get(0).getTitle());
        assertEquals(publisher, partial.get(0).getPublisher());
        assertEquals(first.getPrice(), partial.get(0).getPrice());
        assertNull(partial.get(0).getBinding());
        assertNull(partial.get(0).getCategory());
        assertNull(partial.get(0).getCreateTime());
        List<Map<String, Object>> maps = mapper.selectMaps(scope()
                .select("book_title", "publisher", "price").orderByAsc("book_id"));
        assertEquals(3, maps.size());
        assertEquals(3, maps.get(0).size());
        assertEquals(first.getTitle(), maps.get(0).get("book_title"));
        maps.forEach(System.out::println);
    }

    @Test
    public void publisherGroupingCountsBooksAndAveragesPrice() {
        heading("分组聚合：出版社图书数量与平均定价");
        Map<String, Object> group = mapper.summarizePublishers().stream()
                .filter(row -> publisher.equals(row.get("publisher"))).findFirst().orElseThrow(AssertionError::new);
        assertEquals(2L, ((Number) group.get("book_count")).longValue());
        assertEquals(0, new BigDecimal("140.00").compareTo((BigDecimal) group.get("average_price")));
        System.out.println(group);
        assertEquals(1, mapper.deleteById(second.getId()));
        Map<String, Object> remaining = mapper.summarizePublishers().stream()
                .filter(row -> publisher.equals(row.get("publisher"))).findFirst().orElseThrow(AssertionError::new);
        assertEquals(1L, ((Number) remaining.get("book_count")).longValue());
        assertEquals(0, first.getPrice().compareTo((BigDecimal) remaining.get("average_price")));
    }

    @Test
    public void mapEqualityUsesBookColumnsAndFiltersDeletedRows() {
        heading("Map等值查询：图书书名与出版社");
        Map<String, Object> conditions = new HashMap<>();
        conditions.put("book_title", first.getTitle());
        conditions.put("publisher", publisher);
        conditions.put("status", 1);
        List<Book> found = mapper.selectByMap(conditions);
        assertEquals(1, found.size());
        assertEquals(first.getId(), found.get(0).getId());
        System.out.println(found.get(0));
        conditions.put("status", 0);
        assertTrue(mapper.selectByMap(conditions).isEmpty());
        conditions.put("status", 1);
        assertEquals(1, mapper.deleteById(first.getId()));
        assertTrue(mapper.selectByMap(conditions).isEmpty());
    }

    private QueryWrapper<Book> scope() {
        return new QueryWrapper<Book>().likeRight("book_title", titlePrefix);
    }

    private static Book book(String title, String publisher, String price, int status) {
        Book book = new Book();
        book.setTitle(title);
        book.setBinding("精");
        book.setPublisher(publisher);
        book.setCategory("编程技术");
        book.setPrice(new BigDecimal(price));
        book.setPublishedOn(java.sql.Date.valueOf("2026-10-15"));
        book.setSaleStatus(status);
        return book;
    }

    private static void heading(String title) {
        System.out.println("\n========== " + title + " ==========");
    }
}
