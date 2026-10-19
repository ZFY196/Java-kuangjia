package cn.edu.course.mybatis.test;

import cn.edu.course.mybatis.entity.Book;
import cn.edu.course.mybatis.mapper.BookMapper;
import cn.edu.course.mybatis.util.MyBatisUtil;
import org.apache.ibatis.session.SqlSession;
import org.junit.After;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class BookMapperTest {
    private Integer temporaryKey;

    @Test
    public void testInsert() {
        Book book = newBook();
        try (SqlSession session = MyBatisUtil.openClassicSession()) {
            int rows = session.getMapper(BookMapper.class).createRecord(book);
            temporaryKey = book.getId();
            session.commit();

            assertEquals(1, rows);
            assertNotNull("新增后应回填自增主键", temporaryKey);
            System.out.println("新增图书“MyBatis实战实验”，回填编号 id = " + temporaryKey);
        }

        // 在新会话中查询，确认提交后的记录已保存到数据库。
        try (SqlSession session = MyBatisUtil.openClassicSession()) {
            Book saved = session.getMapper(BookMapper.class).findRecord(temporaryKey);
            assertNotNull(saved);
            assertEquals("MyBatis实战实验", saved.getTitle());
            assertEquals("精", saved.getBinding());
            assertEquals("数据实验出版社", saved.getPublisher());
            assertEquals("软件测试", saved.getCategory());
            assertEquals(0, new BigDecimal("118.60").compareTo(saved.getPrice()));
            assertEquals(Integer.valueOf(1), saved.getSaleStatus());
        }
    }

    @Test
    public void testUpdate() {
        insertFixture();
        try (SqlSession session = MyBatisUtil.openClassicSession()) {
            BookMapper mapper = session.getMapper(BookMapper.class);
            Book book = mapper.findRecord(temporaryKey);
            assertNotNull(book);
            book.setPrice(new BigDecimal("146.80"));

            int rows = mapper.replaceDetails(book);
            session.commit();
            assertEquals(1, rows);
        }

        try (SqlSession session = MyBatisUtil.openClassicSession()) {
            Book updated = session.getMapper(BookMapper.class).findRecord(temporaryKey);
            assertNotNull(updated);
            assertEquals(0, new BigDecimal("146.80").compareTo(updated.getPrice()));
            assertEquals("MyBatis实战实验", updated.getTitle());
        }
    }

    @Test
    public void testDelete() {
        insertFixture();
        try (SqlSession session = MyBatisUtil.openClassicSession()) {
            int rows = session.getMapper(BookMapper.class).removeRecord(temporaryKey);
            session.commit();
            assertEquals(1, rows);
        }

        try (SqlSession session = MyBatisUtil.openClassicSession()) {
            assertNull(session.getMapper(BookMapper.class).findRecord(temporaryKey));
        }
    }

    @After
    public void cleanUpTestBook() throws java.sql.SQLException {
        if (temporaryKey != null) {
            try (SqlSession session = MyBatisUtil.openClassicSession()) {
                // 清理仅由本测试创建的记录；业务删除仍通过逻辑删除执行。
                try (java.sql.PreparedStatement statement = session.getConnection()
                        .prepareStatement("DELETE FROM book WHERE book_id = ?")) {
                    statement.setInt(1, temporaryKey);
                    statement.executeUpdate();
                    session.commit(true);
                }
            }
        }
    }

    private void insertFixture() {
        Book book = newBook();
        try (SqlSession session = MyBatisUtil.openClassicSession()) {
            int rows = session.getMapper(BookMapper.class).createRecord(book);
            temporaryKey = book.getId();
            session.commit();
            assertEquals(1, rows);
            assertNotNull(temporaryKey);
        }
    }

    private Book newBook() {
        Book book = new Book();
        book.setTitle("MyBatis实战实验");
        book.setBinding("精");
        book.setPublisher("数据实验出版社");
        book.setCategory("软件测试");
        book.setPrice(new BigDecimal("118.60"));
        book.setPublishedOn(new Date());
        book.setSaleStatus(1);
        return book;
    }
}
