package cn.edu.course.mybatis.mapper;

import cn.edu.course.mybatis.entity.Book;
import cn.edu.course.mybatis.entity.BookSearch;
import cn.edu.course.mybatis.util.BookQueryWrappers;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Collection;
import java.util.Map;

public interface BookMapper extends BaseMapper<Book> {
    List<Book> listBooks();

    Book findRecord(@Param("key") Integer bookKey);

    int createRecord(@Param("record") Book book);

    int removeRecord(@Param("key") Integer bookKey);

    int replaceDetails(@Param("record") Book book);

    List<Book> searchBooks(@Param("criteria") BookSearch request);

    List<Book> searchBooksWithTrim(@Param("criteria") BookSearch request);

    int patchDetails(@Param("change") Book changes);

    int removeBooks(@Param("bookKeys") Collection<Integer> keys);

    int addBooks(@Param("newcomers") List<Book> records);

    List<Book> findPreferred(@Param("criteria") BookSearch request);

    /** 部分字段查询：只返回编号、书名、出版社和定价。 */
    default List<Book> selectBriefBooks(BookSearch request) {
        return selectList(BookQueryWrappers.byProperties(request)
                .select(Book::getId, Book::getTitle, Book::getPublisher, Book::getPrice));
    }

    /** 按出版社统计正常图书数量与平均定价；下架记录可由 status 条件单独筛选。 */
    default List<Map<String, Object>> summarizePublishers() {
        return selectMaps(new QueryWrapper<Book>()
                .select("publisher", "COUNT(*) AS book_count", "AVG(price) AS average_price")
                .groupBy("publisher").orderByAsc("publisher"));
    }
}
