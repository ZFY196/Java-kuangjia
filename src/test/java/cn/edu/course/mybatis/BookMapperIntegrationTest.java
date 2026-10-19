package cn.edu.course.mybatis;

import cn.edu.course.mybatis.entity.Book;
import cn.edu.course.mybatis.mapper.BookMapper;
import cn.edu.course.mybatis.util.MyBatisUtil;
import org.apache.ibatis.session.SqlSession;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Date;

import static org.junit.Assert.*;

public class BookMapperIntegrationTest {
    @Test
    public void crudMapsAllFieldsAndReturnsGeneratedKey() {
        try (SqlSession session = MyBatisUtil.openSession()) {
            try {
                BookMapper mapper = session.getMapper(BookMapper.class);
                int originalCount = mapper.listBooks().size();
                Book book = new Book();
                book.setTitle("映射测试图书");
                book.setBinding("精");
                book.setPublisher("测试出版社");
                book.setCategory("软件测试");
                book.setPrice(new BigDecimal("88.00"));
                book.setPublishedOn(Date.valueOf("2026-10-14"));
                book.setSaleStatus(1);

                assertEquals(1, mapper.insert(book));
                assertNotNull(book.getId());
                assertEquals(originalCount + 1, mapper.listBooks().size());
                assertMappedFields(book, mapper.selectById(book.getId()));

                book.setTitle("更新后的测试图书");
                book.setBinding("平");
                book.setPublisher("质量实验出版社");
                book.setCategory("测试管理");
                book.setPrice(new BigDecimal("105.01"));
                book.setSaleStatus(0);
                assertEquals(1, mapper.replaceDetails(book));
                assertMappedFields(book, mapper.selectById(book.getId()));

                assertEquals(1, mapper.deleteById(book.getId()));
                assertNull(mapper.selectById(book.getId()));
                assertEquals(originalCount, mapper.listBooks().size());
            } finally {
                session.rollback();
            }
        }
    }

    @Test
    public void rollbackDoesNotPersistInsertedBook() {
        Integer bookId;
        try (SqlSession session = MyBatisUtil.openSession()) {
            Book book = new Book();
            book.setTitle("事务回滚测试图书");
            book.setBinding("精");
            book.setPublisher("测试出版社");
            book.setCategory("软件测试");
            book.setPrice(new BigDecimal("88.00"));
            book.setPublishedOn(Date.valueOf("2026-10-14"));
            book.setSaleStatus(1);
            assertEquals(1, session.getMapper(BookMapper.class).insert(book));
            bookId = book.getId();
            assertNotNull(bookId);
            session.rollback();
        }
        try (SqlSession session = MyBatisUtil.openSession()) {
            assertNull(session.getMapper(BookMapper.class).selectById(bookId));
        }
    }

    private static void assertMappedFields(Book expected, Book actual) {
        assertNotNull(actual);
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getTitle(), actual.getTitle());
        assertEquals(expected.getBinding(), actual.getBinding());
        assertEquals(expected.getPublisher(), actual.getPublisher());
        assertEquals(expected.getCategory(), actual.getCategory());
        assertEquals(expected.getPrice(), actual.getPrice());
        assertEquals(expected.getPublishedOn().getTime(), actual.getPublishedOn().getTime());
        assertEquals(expected.getSaleStatus(), actual.getSaleStatus());
    }
}
