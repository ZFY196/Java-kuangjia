package cn.edu.course.mybatis.test;

import cn.edu.course.mybatis.entity.Publisher;
import cn.edu.course.mybatis.entity.Book;
import cn.edu.course.mybatis.entity.Author;
import cn.edu.course.mybatis.mapper.MultiTableMapper;
import cn.edu.course.mybatis.mapper.BookMapper;
import cn.edu.course.mybatis.util.MyBatisUtil;
import org.apache.ibatis.session.SqlSession;
import java.util.List;

/**
 * 多表联合查询演示：一对一、多对一、一对多、双向多对多。
 *
 * 运行前请先执行 sql/book_relations_init.sql 建表并灌入样例数据。
 * 可通过命令行参数指定查询编号：图书编号 [出版社编号] [作者编号]。
 * 未指定时选取实际存在的图书，以及该图书关联的出版社和首个作者。
 */
public class MultiTableRelationDemo {
    public static void main(String[] arguments) {
        try (SqlSession session = MyBatisUtil.openClassicSession()) {
            MultiTableMapper repository = session.getMapper(MultiTableMapper.class);
            Integer bookKey;
            if (arguments.length >= 1) {
                bookKey = Integer.valueOf(arguments[0]);
            } else {
                List<Book> catalog = session.getMapper(BookMapper.class).listBooks();
                if (catalog.isEmpty()) {
                    System.out.println("暂无图书，请先执行 sql/book_init.sql，再执行 sql/book_relations_init.sql。");
                    return;
                }
                bookKey = catalog.get(0).getId();
            }
            Book withPublisher = repository.findBookWithPublisher(bookKey);
            Book withAuthors = repository.findBookWithAuthors(bookKey);
            Integer publisherKey = arguments.length >= 2 ? Integer.valueOf(arguments[1])
                    : withPublisher != null && withPublisher.getPublisherInfo() != null
                    ? withPublisher.getPublisherInfo().getPublisherId() : null;
            Integer authorKey = arguments.length >= 3 ? Integer.valueOf(arguments[2])
                    : withAuthors != null && !withAuthors.getAuthors().isEmpty()
                    ? withAuthors.getAuthors().get(0).getAuthorId() : null;

            System.out.println("== 一对一：图书 及其 书籍详情（book_id=" + bookKey + "）==");
            Book withCard = repository.findBookWithDetail(bookKey);
            System.out.println(withCard == null ? "图书不存在" : withCard);
            if (withCard != null) {
                System.out.println("  详情 → " + (withCard.getDetail() == null ? "未关联书籍详情" : withCard.getDetail()));
            }

            System.out.println("\n== 多对一：图书 及其 所属出版社（book_id=" + bookKey + "）==");
            if (withPublisher == null) {
                System.out.println("图书不存在");
            } else {
                System.out.println(withPublisher);
                System.out.println("  所属出版社 → " + (withPublisher.getPublisherInfo() == null ? "未关联有效出版社" : withPublisher.getPublisherInfo()));
            }

            System.out.println("\n== 一对多：出版社 及其 图书列表（publisherId=" + publisherKey + "）==");
            Publisher publisher = publisherKey == null ? null : repository.findPublisherWithBooks(publisherKey);
            if (publisherKey == null) {
                System.out.println("该图书未关联有效出版社，请通过第二个参数指定出版社编号");
            } else if (publisher == null) {
                System.out.println("出版社不存在");
            } else {
                System.out.println(publisher);
                if (publisher.getBooks().isEmpty()) {
                    System.out.println("  暂无图书");
                }
                for (Book member : publisher.getBooks()) {
                    System.out.println("  图书 → " + member);
                }
            }

            System.out.println("\n== 多对多·方向一：通过作者查图书（author_id=" + authorKey + "）==");
            Author author = authorKey == null ? null : repository.findAuthorWithBooks(authorKey);
            if (authorKey == null) {
                System.out.println("该图书暂未关联作者，请通过第三个参数指定作者编号");
            } else if (author == null) {
                System.out.println("作者不存在");
            } else {
                System.out.println(author);
                if (author.getBooks().isEmpty()) {
                    System.out.println("  暂无关联图书");
                }
                for (Book member : author.getBooks()) {
                    System.out.println("  关联图书 → " + member);
                }
            }

            System.out.println("\n== 多对多·方向二：通过图书查作者（book_id=" + bookKey + "）==");
            if (withAuthors == null) {
                System.out.println("图书不存在");
            } else {
                System.out.println(withAuthors);
                if (withAuthors.getAuthors().isEmpty()) {
                    System.out.println("  暂未关联作者");
                }
                for (Author joined : withAuthors.getAuthors()) {
                    System.out.println("  关联作者 → " + joined);
                }
            }
        }
    }
}
