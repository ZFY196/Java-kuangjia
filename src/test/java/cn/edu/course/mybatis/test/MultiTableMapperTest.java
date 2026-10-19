package cn.edu.course.mybatis.test;

import cn.edu.course.mybatis.entity.Publisher;
import cn.edu.course.mybatis.entity.Book;
import cn.edu.course.mybatis.entity.Author;
import cn.edu.course.mybatis.mapper.MultiTableMapper;
import cn.edu.course.mybatis.mapper.BookMapper;
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
    private int publisherId;
    private int firstBook;
    private int secondBook;
    private int firstAuthor;
    private int secondAuthor;

    @Before
    public void setUp() throws SQLException {
        session = MyBatisUtil.openClassicSession();
        mapper = session.getMapper(MultiTableMapper.class);
        try {
            String suffix = Long.toString(System.nanoTime());
            publisherId = insert("INSERT INTO publisher(publisher_name, city, deleted) VALUES (?, '上海', 0)", "关联测试出版社" + suffix);
            firstBook = insert("INSERT INTO book(book_title, binding, publisher, publisher_id, status) VALUES (?, '精', ?, ?, 1)",
                    "关联测试图书甲", "关联测试出版社" + suffix, publisherId);
            secondBook = insert("INSERT INTO book(book_title, binding, publisher, publisher_id, status) VALUES (?, '平', ?, ?, 1)",
                    "关联测试图书乙", "关联测试出版社" + suffix, publisherId);
            firstAuthor = insert("INSERT INTO author(author_name, birth_date) VALUES (?, '1984-10-15')", "关联测试作者甲" + suffix);
            secondAuthor = insert("INSERT INTO author(author_name, birth_date) VALUES (?, '1988-06-20')", "关联测试作者乙" + suffix);
            insert("INSERT INTO book_detail(book_id, isbn, print_date) VALUES (?, ?, '2025-10-15')", firstBook, "TEST-" + suffix);
            execute("INSERT INTO book_author(book_id, author_id) VALUES (?, ?), (?, ?), (?, ?)",
                    firstBook, firstAuthor, firstBook, secondAuthor, secondBook, firstAuthor);
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
    public void xmlCrudKeepsPublisherForeignKeyAndAssociationTogether() throws SQLException {
        BookMapper books = session.getMapper(BookMapper.class);
        Book book = new Book();
        book.setTitle("出版社关联维护实验");
        book.setPublisherId(publisherId);
        book.setPublisher("关联测试出版社");
        book.setSaleStatus(1);
        assertEquals(1, books.createRecord(book));
        assertEquals(Integer.valueOf(publisherId), books.findRecord(book.getId()).getPublisherId());
        assertEquals(Integer.valueOf(publisherId), mapper.findBookWithPublisher(book.getId())
                .getPublisherInfo().getPublisherId());

        String revisedName = "修订版出版社" + System.nanoTime();
        int revisedId = insert("INSERT INTO publisher(publisher_name) VALUES (?)", revisedName);
        Book patch = new Book();
        patch.setId(book.getId());
        patch.setPublisher(revisedName);
        patch.setPublisherId(revisedId);
        assertEquals(1, books.patchDetails(patch));
        Book revised = mapper.findBookWithPublisher(book.getId());
        assertEquals(Integer.valueOf(revisedId), revised.getPublisherId());
        assertEquals(revisedName, revised.getPublisherInfo().getPublisherName());
    }

    @Test
    public void mapsAllRelationsAndAggregatesEachCollection() {
        Book withCard = mapper.findBookWithDetail(firstBook);
        assertEquals("关联测试图书甲", withCard.getTitle());
        assertNotNull(withCard.getDetail());
        assertEquals(Integer.valueOf(firstBook), withCard.getDetail().getBookId());

        Book withPublisher = mapper.findBookWithPublisher(firstBook);
        assertEquals(Integer.valueOf(publisherId), withPublisher.getPublisherInfo().getPublisherId());
        assertEquals(withPublisher.getPublisher(), withPublisher.getPublisherInfo().getPublisherName());
        assertEquals("上海", withPublisher.getPublisherInfo().getCity());
        assertEquals(withPublisher.getPublisherInfo().getPublisherId(), mapper.findBookWithPublisher(secondBook).getPublisherInfo().getPublisherId());

        Publisher publisher = mapper.findPublisherWithBooks(publisherId);
        assertEquals(Arrays.asList(firstBook, secondBook), publisher.getBooks().stream().map(Book::getId).collect(Collectors.toList()));
        Author author = mapper.findAuthorWithBooks(firstAuthor);
        assertEquals(Arrays.asList(firstBook, secondBook), author.getBooks().stream().map(Book::getId).collect(Collectors.toList()));
        Book book = mapper.findBookWithAuthors(firstBook);
        assertEquals(Arrays.asList(firstAuthor, secondAuthor), book.getAuthors().stream().map(Author::getAuthorId).collect(Collectors.toList()));
    }

    @Test
    public void keepsParentsWithNoChildrenAndOmitsDeletedPublisher() throws SQLException {
        int unassigned = insert("INSERT INTO book(book_title) VALUES (?)", "无关联测试图书");
        int emptyPublisher = insert("INSERT INTO publisher(publisher_name, deleted) VALUES (?, 0)", "空测试出版社" + System.nanoTime());
        int emptyAuthor = insert("INSERT INTO author(author_name) VALUES (?)", "空测试作者" + System.nanoTime());
        assertNull(mapper.findBookWithDetail(unassigned).getDetail());
        assertNull(mapper.findBookWithPublisher(unassigned).getPublisherInfo());
        assertTrue(mapper.findBookWithAuthors(unassigned).getAuthors().isEmpty());
        assertTrue(mapper.findPublisherWithBooks(emptyPublisher).getBooks().isEmpty());
        assertTrue(mapper.findAuthorWithBooks(emptyAuthor).getBooks().isEmpty());

        execute("UPDATE publisher SET deleted = 1 WHERE publisher_id = ?", publisherId);
        assertNull(mapper.findBookWithPublisher(firstBook).getPublisherInfo());
        assertNull(mapper.findPublisherWithBooks(publisherId));
    }

    @Test
    public void missingParentsReturnNull() {
        assertNull(mapper.findBookWithDetail(-1));
        assertNull(mapper.findBookWithPublisher(-1));
        assertNull(mapper.findPublisherWithBooks(-1));
        assertNull(mapper.findAuthorWithBooks(-1));
        assertNull(mapper.findBookWithAuthors(-1));
    }

    @Test
    public void deletedBookIsExcludedFromAllRelations() {
        cn.edu.course.mybatis.mapper.BookMapper books = session.getMapper(cn.edu.course.mybatis.mapper.BookMapper.class);
        assertEquals(1, books.removeRecord(firstBook));
        assertNull(mapper.findBookWithDetail(firstBook));
        assertNull(mapper.findBookWithPublisher(firstBook));
        assertNull(mapper.findBookWithAuthors(firstBook));
        assertEquals(1, mapper.findPublisherWithBooks(publisherId).getBooks().size());
        assertEquals(Integer.valueOf(secondBook), mapper.findAuthorWithBooks(firstAuthor).getBooks().get(0).getId());
        assertEquals(1, books.removeRecord(secondBook));
        assertTrue(mapper.findPublisherWithBooks(publisherId).getBooks().isEmpty());
        assertTrue(mapper.findAuthorWithBooks(firstAuthor).getBooks().isEmpty());
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
