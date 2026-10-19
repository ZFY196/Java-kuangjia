package cn.edu.course.mybatis.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 书籍详情实体类（book_detail 表）
 *
 * 与 book 表是一对一关系：一本图书只有一份详情（book_detail.book_id 唯一约束）
 */
@TableName("book_detail")
public class BookDetail implements Serializable {

    @TableId(value = "detail_id", type = IdType.AUTO)
    private Integer detailId;      // 详情编号（主键）

    @TableField("book_id")
    private Integer bookId;         // 图书编号（唯一外键）

    @TableField("isbn")
    private String isbn;         // ISBN

    @TableField("print_date")
    private Date printDate;       // 印刷日期

    public Integer getDetailId() {
        return detailId;
    }

    public void setDetailId(Integer detailId) {
        this.detailId = detailId;
    }

    public Integer getBookId() {
        return bookId;
    }

    public void setBookId(Integer bookId) {
        this.bookId = bookId;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public Date getPrintDate() {
        return printDate;
    }

    public void setPrintDate(Date printDate) {
        this.printDate = printDate;
    }

    @Override
    public String toString() {
        return "书籍详情{" +
                "详情编号=" + detailId +
                ", 图书编号=" + bookId +
                ", ISBN='" + isbn + '\'' +
                ", 印刷日期=" + (printDate == null ? "未填写" : new SimpleDateFormat("yyyy年MM月dd日").format(printDate)) +
                '}';
    }
}
