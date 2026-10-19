package cn.edu.course.mybatis.util;

import cn.edu.course.mybatis.entity.Book;
import java.math.BigDecimal;
import java.util.List;

public final class BookConsole {
    private BookConsole() {
    }

    public static void show(String caption, List<Book> catalog) {
        BigDecimal totalPrice = BigDecimal.ZERO;
        System.out.printf("%n%s，共 %d 本图书%n", caption, catalog.size());
        int ordinal = 1;
        for (Book book : catalog) {
            System.out.printf("%02d. %s%n", ordinal++, book);
            if (book.getPrice() != null) {
                totalPrice = totalPrice.add(book.getPrice());
            }
        }
        System.out.printf("本次结果定价合计：%s 元%n", totalPrice.toPlainString());
    }
}
