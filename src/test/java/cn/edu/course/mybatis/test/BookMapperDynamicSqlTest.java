package cn.edu.course.mybatis.test;

import cn.edu.course.mybatis.entity.Book;
import cn.edu.course.mybatis.entity.BookSearch;
import cn.edu.course.mybatis.mapper.BookMapper;
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

public class BookMapperDynamicSqlTest {
    private SqlSession session;
    private BookMapper mapper;
    private Book activeBook;
    private Book offSaleBook;
    private Book platformBook;

    @Before
    public void setUp() {
        session = MyBatisUtil.openSession();
        mapper = session.getMapper(BookMapper.class);
        activeBook = book("算法入门（上）", "精", "算法实验出版社", "算法设计", "126.40", 1);
        offSaleBook = book("算法入门（下）", "精", "算法实验出版社", "数据科学", "97.20", 0);
        platformBook = book("Linux运维指南", "平", "运维实验出版社", "系统运维", "137.50", 1);
        for (Book book : Arrays.asList(activeBook, offSaleBook, platformBook)) {
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
    public void conditionQueryCombinesNameBindingPublisherAndStatus() {
        BookSearch filter = new BookSearch();
        filter.setTitleText("算法入门");
        assertEquals(Arrays.asList(activeBook.getId(), offSaleBook.getId()),
                ids(mapper.searchBooks(filter)));

        filter.setBinding("精");
        filter.setPublisher("算法实验出版社");
        filter.setSaleStatus(1);
        assertEquals(Collections.singletonList(activeBook.getId()), ids(mapper.searchBooks(filter)));
        filter.setSaleStatus(0);
        assertEquals(Collections.singletonList(offSaleBook.getId()), ids(mapper.searchBooks(filter)));
        filter.setBinding("平");
        assertTrue(mapper.searchBooks(filter).isEmpty());

        BookSearch publisherOnly = new BookSearch();
        publisherOnly.setPublisher("运维实验出版社");
        assertEquals(Collections.singletonList(platformBook.getId()),
                ids(mapper.searchBooks(publisherOnly)));
        publisherOnly.setBinding("精");
        assertTrue(mapper.searchBooks(publisherOnly).isEmpty());
    }

    @Test
    public void trimAndWhereReturnTheSameRowsIncludingBooktyConditions() {
        BookSearch blank = new BookSearch();
        blank.setTitleText("");
        blank.setBinding("");
        blank.setPublisher("");
        List<Integer> allIds = ids(mapper.listBooks());
        assertEquals(allIds, ids(mapper.searchBooks(null)));
        assertEquals(allIds, ids(mapper.searchBooks(blank)));

        BookSearch nameOnly = new BookSearch();
        nameOnly.setTitleText("算法入门");
        BookSearch bindingOnly = new BookSearch();
        bindingOnly.setBinding("平");
        BookSearch publisherOnly = new BookSearch();
        publisherOnly.setPublisher("算法实验出版社");
        BookSearch statusOnly = new BookSearch();
        statusOnly.setSaleStatus(0);
        BookSearch combined = new BookSearch();
        combined.setTitleText("算法入门");
        combined.setBinding("精");
        combined.setPublisher("算法实验出版社");
        combined.setSaleStatus(1);

        for (BookSearch filter : Arrays.asList(null, blank, nameOnly, bindingOnly, publisherOnly, statusOnly, combined)) {
            assertEquals(ids(mapper.searchBooks(filter)), ids(mapper.searchBooksWithTrim(filter)));
        }
    }

    @Test
    public void dynamicUpdateChangesOnlyProvidedFieldsAndAcceptsZeroValues() {
        Book original = mapper.selectById(activeBook.getId());
        Book patch = new Book();
        patch.setId(original.getId());
        patch.setPrice(new BigDecimal("0.00"));
        patch.setSaleStatus(0);
        assertEquals(1, mapper.patchDetails(patch));

        Book updated = mapper.selectById(original.getId());
        assertEquals(0, updated.getPrice().compareTo(BigDecimal.ZERO));
        assertEquals(Integer.valueOf(0), updated.getSaleStatus());
        assertEquals(original.getTitle(), updated.getTitle());
        assertEquals(original.getBinding(), updated.getBinding());
        assertEquals(original.getPublisher(), updated.getPublisher());
        assertEquals(original.getCategory(), updated.getCategory());
        assertEquals(original.getPublishedOn(), updated.getPublishedOn());

        Book detailsPatch = new Book();
        detailsPatch.setId(original.getId());
        detailsPatch.setTitle("算法入门修订版");
        detailsPatch.setPublisher("数据实验出版社");
        detailsPatch.setCategory("数据分析");
        assertEquals(1, mapper.patchDetails(detailsPatch));
        Book details = mapper.selectById(original.getId());
        assertEquals("算法入门修订版", details.getTitle());
        assertEquals("数据实验出版社", details.getPublisher());
        assertEquals("数据分析", details.getCategory());
        assertEquals(0, details.getPrice().compareTo(BigDecimal.ZERO));
        assertEquals(Integer.valueOf(0), details.getSaleStatus());
        assertEquals(original.getBinding(), details.getBinding());
        assertEquals(original.getPublishedOn(), details.getPublishedOn());
    }

    @Test
    public void dynamicUpdateWithoutFieldsOrIdDoesNotChangeRows() {
        List<String> before = snapshots(mapper.listBooks());
        Book noFields = new Book();
        noFields.setId(activeBook.getId());
        assertEquals(0, mapper.patchDetails(noFields));
        Book noId = new Book();
        noId.setPrice(new BigDecimal("1.00"));
        assertEquals(0, mapper.patchDetails(noId));
        assertEquals(0, mapper.patchDetails(null));
        assertEquals(before, snapshots(mapper.listBooks()));
    }

    @Test
    public void batchInsertReturnsKeysAndBatchDeleteRemovesOnlyRequestedRows() {
        List<String> before = snapshots(mapper.listBooks());
        Book first = book("Spring开发入门", "平", "集成实验出版社", "应用开发", "142.30", 1);
        Book second = book("数据库设计实践", "精", "集成实验出版社", "数据库", "108.90", 0);
        assertEquals(2, mapper.addBooks(Arrays.asList(first, second)));
        assertNotNull(first.getId());
        assertNotNull(second.getId());
        assertNotEquals(first.getId(), second.getId());
        assertEquals("Spring开发入门", mapper.selectById(first.getId()).getTitle());
        Book savedSecond = mapper.selectById(second.getId());
        assertEquals("数据库设计实践", savedSecond.getTitle());
        assertEquals(second.getPrice(), savedSecond.getPrice());
        assertEquals(second.getSaleStatus(), savedSecond.getSaleStatus());

        assertEquals(2, mapper.removeBooks(Arrays.asList(first.getId(), second.getId())));
        assertNull(mapper.selectById(first.getId()));
        assertNull(mapper.selectById(second.getId()));
        assertEquals(before, snapshots(mapper.listBooks()));
    }

    @Test
    public void emptyBatchOperationsDoNotChangeRows() {
        List<String> before = snapshots(mapper.listBooks());
        assertEquals(0, mapper.removeBooks(Collections.emptyList()));
        assertEquals(0, mapper.removeBooks(null));
        assertEquals(0, mapper.addBooks(Collections.emptyList()));
        assertEquals(0, mapper.addBooks(null));
        assertEquals(before, snapshots(mapper.listBooks()));
    }

    @Test
    public void normalizedSearchCriteriaAgreeAcrossXmlAndPlus() {
        BookSearch request = new BookSearch().setTitleText("  算法入门  ")
                .setBinding(" 精 ").setPublisher(" 算法实验出版社 ")
                .setMinimumPrice(new BigDecimal("100.00"));
        List<Integer> expected = Collections.singletonList(activeBook.getId());
        assertEquals(expected, ids(mapper.searchBooks(request)));
        assertEquals(expected, ids(mapper.searchBooksWithTrim(request)));
        assertEquals(expected, ids(mapper.selectList(
                cn.edu.course.mybatis.util.BookQueryWrappers.byColumns(request))));
        assertEquals(expected, ids(mapper.selectList(
                cn.edu.course.mybatis.util.BookQueryWrappers.byProperties(request))));

        request.setTitleText("  ").setBinding(" ").setPublisher(null).setMinimumPrice(null);
        assertEquals(ids(mapper.listBooks()), ids(mapper.searchBooks(request)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void searchRejectsNegativeMinimumPrice() {
        new BookSearch().setMinimumPrice(new BigDecimal("-0.01"));
    }

    @Test
    public void choosePrioritizesExactNameThenPublisherThenAllRows() {
        BookSearch query = new BookSearch();
        query.setTitleText("算法入门（上）");
        query.setPublisher("运维实验出版社");
        assertEquals(Collections.singletonList(activeBook.getId()), ids(mapper.findPreferred(query)));

        query.setTitleText("算法入门");
        assertTrue(mapper.findPreferred(query).isEmpty());
        assertTrue(mapper.searchBooks(query).isEmpty());
        query.setTitleText("算法入门（上）");
        assertTrue(mapper.searchBooks(query).isEmpty());

        query.setTitleText("");
        query.setPublisher("算法实验出版社");
        query.setBinding("平");
        query.setSaleStatus(0);
        assertEquals(Arrays.asList(activeBook.getId(), offSaleBook.getId()),
                ids(mapper.findPreferred(query)));

        query.setPublisher("");
        assertEquals(ids(mapper.listBooks()), ids(mapper.findPreferred(query)));
        assertEquals(ids(mapper.listBooks()), ids(mapper.findPreferred(null)));
    }

    private static Book book(String title, String binding, String publisher, String category,
                                String price, int status) {
        Book book = new Book();
        book.setTitle(title);
        book.setBinding(binding);
        book.setPublisher(publisher);
        book.setCategory(category);
        book.setPrice(new BigDecimal(price));
        book.setPublishedOn(Date.valueOf("2026-05-18"));
        book.setSaleStatus(status);
        return book;
    }

    private static List<Integer> ids(List<Book> books) {
        return books.stream().map(Book::getId).collect(Collectors.toList());
    }

    private static List<String> snapshots(List<Book> books) {
        return books.stream().map(Book::toString).collect(Collectors.toList());
    }
}
