package cn.edu.course.mybatis.entity;

import java.math.BigDecimal;

/** 查询条件独立于持久化实体；空白文字统一按未指定处理。 */
public final class BookSearch {
    private String titleText;
    private String binding;
    private String publisher;
    private Integer saleStatus;
    private BigDecimal minimumPrice;

    public static BookSearch availableBooks() {
        return new BookSearch().setSaleStatus(1);
    }

    public String getTitleText() {
        return titleText;
    }

    public BookSearch setTitleText(String text) {
        titleText = clean(text);
        return this;
    }

    public String getBinding() {
        return binding;
    }

    public BookSearch setBinding(String value) {
        binding = clean(value);
        return this;
    }

    public String getPublisher() {
        return publisher;
    }

    public BookSearch setPublisher(String value) {
        publisher = clean(value);
        return this;
    }

    public Integer getSaleStatus() {
        return saleStatus;
    }

    public BookSearch setSaleStatus(Integer value) {
        saleStatus = value;
        return this;
    }

    public BigDecimal getMinimumPrice() {
        return minimumPrice;
    }

    public BookSearch setMinimumPrice(BigDecimal value) {
        if (value != null && value.signum() < 0) {
            throw new IllegalArgumentException("最低定价不能为负数");
        }
        minimumPrice = value;
        return this;
    }

    private static String clean(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
