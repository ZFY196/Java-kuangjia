package cn.edu.course.mybatis.mapper;

import cn.edu.course.mybatis.entity.Publisher;
import cn.edu.course.mybatis.entity.Book;
import cn.edu.course.mybatis.entity.Author;
import org.apache.ibatis.annotations.Param;

/**
 * 多表联合查询 Mapper（XML 实现，见 resources 下同名 MultiTableMapper.xml）
 *
 * 覆盖以下关联查询：
 *   一对一：book 与 book_detail（association 嵌套）
 *   一对多：publisher 与 book（collection 嵌套）
 *   多对一：book 与 publisher（association 嵌套）
 *   多对多：author 与 book（正向：通过作者查图书）
 *   多对多：book 与 author（反向：通过图书查作者）
 */
public interface MultiTableMapper {

    /** 一对一：按图书编号查询图书及其书籍详情信息 */
    Book findBookWithDetail(@Param("bookId") Integer bookId);

    /** 多对一：按图书编号查询图书及其所属出版社 */
    Book findBookWithPublisher(@Param("bookId") Integer bookId);

    /** 一对多：按出版社编号查询出版社及其全部图书 */
    Publisher findPublisherWithBooks(@Param("publisherId") Integer publisherId);

    /** 多对多（方向一）：按作者编号查询作者及其全部关联图书 */
    Author findAuthorWithBooks(@Param("authorId") Integer authorId);

    /** 多对多（方向二）：按图书编号查询图书及其关联的全部作者 */
    Book findBookWithAuthors(@Param("bookId") Integer bookId);
}
