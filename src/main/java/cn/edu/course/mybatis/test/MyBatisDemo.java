package cn.edu.course.mybatis.test;

import cn.edu.course.mybatis.entity.Book;
import cn.edu.course.mybatis.mapper.BookMapper;
import cn.edu.course.mybatis.util.MyBatisUtil;
import cn.edu.course.mybatis.util.BookConsole;
import java.util.Collections;
import java.util.List;

public class MyBatisDemo {
    public static void main(String[] arguments) {
        List<Book> catalog = MyBatisUtil.inspectClassic(BookMapper::listBooks);
        BookConsole.show("普通 MyBatis 图书目录", catalog);
        if (catalog.isEmpty()) {
            return;
        }
        Integer requestedKey = arguments.length == 0 ? catalog.get(0).getId()
                : Integer.valueOf(arguments[0]);
        Book found = MyBatisUtil.inspectClassic(repository -> repository.findRecord(requestedKey));
        BookConsole.show("编号 " + requestedKey + " 的查询结果",
                found == null ? Collections.emptyList() : Collections.singletonList(found));
        MultiTableRelationDemo.main(arguments);
    }
}
