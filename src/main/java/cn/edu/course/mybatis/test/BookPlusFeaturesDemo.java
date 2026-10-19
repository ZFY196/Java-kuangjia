package cn.edu.course.mybatis.test;

import cn.edu.course.mybatis.entity.Book;
import cn.edu.course.mybatis.entity.BookSearch;
import cn.edu.course.mybatis.mapper.BookMapper;
import cn.edu.course.mybatis.util.MyBatisUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.session.SqlSession;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/** 图书七项补充功能演示，新增和修改均在结束时回滚。 */
public class BookPlusFeaturesDemo {
    public static void main(String[] arguments) {
        try (SqlSession session = MyBatisUtil.openSession()) {
            try {
                BookMapper mapper = session.getMapper(BookMapper.class);
                System.out.println("\n== 1 自动填充：新增临时演示图书 ==");
                Book temporary = new Book();
                temporary.setTitle("MP功能演示图书");
                temporary.setBinding("精");
                temporary.setPublisher("实验演示出版社");
                temporary.setCategory("编程技术");
                temporary.setPrice(new BigDecimal("120.00"));
                temporary.setPublishedOn(java.sql.Date.valueOf("2026-10-15"));
                temporary.setSaleStatus(1);
                mapper.insert(temporary);
                Book current = mapper.selectById(temporary.getId());
                System.out.println("创建时间: " + current.getCreateTime() + "，更新时间: " + current.getUpdateTime());

                System.out.println("\n== 2 乐观锁：旧版本不能覆盖新定价 ==");
                session.clearCache();
                Book stale = mapper.selectById(current.getId());
                current.setPrice(new BigDecimal("140.00"));
                System.out.println("当前版本更新影响行数: " + mapper.updateById(current));
                stale.setPrice(new BigDecimal("1.00"));
                System.out.println("过期版本更新影响行数: " + mapper.updateById(stale));
                Book latest = mapper.selectById(current.getId());
                System.out.println("当前版本: " + latest.getVersion() + "，定价: " + latest.getPrice());
                System.out.println("更新后自动填充时间: " + latest.getUpdateTime());

                System.out.println("\n== 3 字段策略：分类null保留原值 ==");
                Book patch = new Book();
                patch.setId(current.getId());
                patch.setPrice(new BigDecimal("150.00"));
                mapper.updateById(patch);
                System.out.println("只更新定价后的分类: " + mapper.selectById(current.getId()).getCategory());
                mapper.update(new Book(), new LambdaUpdateWrapper<Book>()
                        .eq(Book::getId, current.getId()).set(Book::getCategory, null));
                System.out.println("显式set(null)后的分类: " + mapper.selectById(current.getId()).getCategory());

                System.out.println("\n== 4 部分字段查询：编号、书名、出版社、定价 ==");
                mapper.selectBriefBooks(new BookSearch()).forEach(book -> System.out.printf(
                        "编号=%d，书名=%s，出版社=%s，定价=%s%n", book.getId(), book.getTitle(),
                        book.getPublisher(), book.getPrice()));
                System.out.println("\n== 5 分组聚合：出版社图书数量与平均定价 ==");
                mapper.summarizePublishers().forEach(System.out::println);
                System.out.println("\n== 6 Map等值查询：书名与出版社 ==");
                Map<String, Object> conditions = new HashMap<>();
                conditions.put("book_title", temporary.getTitle());
                conditions.put("publisher", temporary.getPublisher());
                conditions.put("book_id", temporary.getId());
                mapper.selectByMap(conditions).forEach(System.out::println);
                System.out.println("\n== 7 逻辑删除：临时图书deleted变为1 ==");
                System.out.println("删除影响行数: " + mapper.deleteById(current.getId()));
                System.out.println("删除后按编号查询: " + mapper.selectById(current.getId()));
                System.out.println("删除后Map查询数量: " + mapper.selectByMap(conditions).size());
            } finally {
                session.rollback(true);
                System.out.println("\n演示事务已回滚，临时记录及修改均未保存。");
            }
        }
    }
}
