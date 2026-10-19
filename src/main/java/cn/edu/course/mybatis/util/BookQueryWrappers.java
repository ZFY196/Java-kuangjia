package cn.edu.course.mybatis.util;

import cn.edu.course.mybatis.entity.Book;
import cn.edu.course.mybatis.entity.BookSearch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.math.BigDecimal;

public final class BookQueryWrappers {
    private BookQueryWrappers() {
    }

    public static QueryWrapper<Book> byColumns(BookSearch request) {
        BookSearch options = request == null ? BookSearch.availableBooks() : request;
        QueryWrapper<Book> result = new QueryWrapper<>();
        result.orderByDesc("price").orderByAsc("book_id");
        if (options.getPublisher() != null) {
            result.eq("publisher", options.getPublisher());
        }
        if (options.getSaleStatus() != null) {
            result.eq("status", options.getSaleStatus());
        }
        if (options.getMinimumPrice() != null) {
            result.ge("price", options.getMinimumPrice());
        }
        if (options.getTitleText() != null) {
            result.like("book_title", options.getTitleText());
        }
        if (options.getBinding() != null) {
            result.eq("binding", options.getBinding());
        }
        return result;
    }

    public static LambdaQueryWrapper<Book> byProperties(BookSearch request) {
        BookSearch options = request == null ? BookSearch.availableBooks() : request;
        LambdaQueryWrapper<Book> result = new LambdaQueryWrapper<>();
        result.orderByDesc(Book::getPrice).orderByAsc(Book::getId);
        if (options.getBinding() != null) {
            result.eq(Book::getBinding, options.getBinding());
        }
        if (options.getTitleText() != null) {
            result.like(Book::getTitle, options.getTitleText());
        }
        if (options.getMinimumPrice() != null) {
            result.ge(Book::getPrice, options.getMinimumPrice());
        }
        if (options.getSaleStatus() != null) {
            result.eq(Book::getSaleStatus, options.getSaleStatus());
        }
        if (options.getPublisher() != null) {
            result.eq(Book::getPublisher, options.getPublisher());
        }
        return result;
    }

    public static QueryWrapper<Book> byColumns(String title, String binding, BigDecimal minimum) {
        return byColumns(BookSearch.availableBooks().setTitleText(title).setBinding(binding).setMinimumPrice(minimum));
    }

    public static LambdaQueryWrapper<Book> byProperties(String title, String binding, BigDecimal minimum) {
        return byProperties(BookSearch.availableBooks().setTitleText(title).setBinding(binding).setMinimumPrice(minimum));
    }
}
