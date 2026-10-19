package cn.edu.course.mybatis.test;

import cn.edu.course.mybatis.entity.Book;
import cn.edu.course.mybatis.entity.BookSearch;
import cn.edu.course.mybatis.util.BookQueryWrappers;
import cn.edu.course.mybatis.util.MyBatisUtil;
import cn.edu.course.mybatis.util.BookConsole;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public class MyBatisPlusDemo {
    public static void main(String[] arguments) {
        BookSearch request = BookSearch.availableBooks();
        if (arguments.length > 0) {
            request.setTitleText(arguments[0]);
        }
        if (arguments.length > 1) {
            request.setBinding(arguments[1]);
        }
        if (arguments.length > 2) {
            request.setMinimumPrice(new java.math.BigDecimal(arguments[2]));
        }
        long pageNumber = arguments.length > 3 ? Long.parseLong(arguments[3]) : 1;
        long pageCapacity = arguments.length > 4 ? Long.parseLong(arguments[4]) : 3;
        if (pageNumber < 1 || pageCapacity < 1) {
            throw new IllegalArgumentException("页码和每页数量必须为正整数");
        }

        MyBatisUtil.inspect(repository -> {
            BookConsole.show("列名条件构造器", repository.selectList(BookQueryWrappers.byColumns(request)));
            BookConsole.show("属性条件构造器", repository.selectList(BookQueryWrappers.byProperties(request)));
            Page<Book> slice = repository.selectPage(new Page<>(pageNumber, pageCapacity),
                    BookQueryWrappers.byProperties(request));
            System.out.printf("%n分页统计：%d 条记录，%d 页；当前第 %d 页，每页 %d 条%n",
                    slice.getTotal(), slice.getPages(), slice.getCurrent(), slice.getSize());
            BookConsole.show("本页图书", slice.getRecords());
            return null;
        });
        BookPlusFeaturesDemo.main(new String[0]);
    }
}
