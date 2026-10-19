package cn.edu.course.mybatis.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 作者实体类（author 表）
 *
 * 与 book 表是多对多关系：一本图书可由多位作者合著，
 * 一位作者也可创作多本图书，通过中间表 book_author 关联。
 */
@TableName("author")
public class Author implements Serializable {

    @TableId(value = "author_id", type = IdType.AUTO)
    private Integer authorId;     // 作者编号（主键）

    @TableField("author_name")
    private String authorName;    // 作者名称

    @TableField("birth_date")
    private Date birthDate;        // 作者出生日期

    // 多对多关系：一个作者有多名关联图书（非数据库列）
    @TableField(exist = false)
    private List<Book> books;

    public Integer getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Integer authorId) {
        this.authorId = authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public List<Book> getBooks() {
        return books;
    }

    public void setBooks(List<Book> books) {
        this.books = books;
    }

    @Override
    public String toString() {
        return "作者{" +
                "作者编号=" + authorId +
                ", 作者名称='" + authorName + '\'' +
                ", 出生日期=" + (birthDate == null ? "未填写" : new SimpleDateFormat("yyyy年MM月dd日").format(birthDate)) +
                '}';
    }
}
