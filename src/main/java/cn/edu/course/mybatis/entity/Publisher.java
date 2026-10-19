package cn.edu.course.mybatis.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableLogic;
import java.io.Serializable;
import java.util.List;

/**
 * 出版社实体类（publisher 表）
 *
 * 与 book 表是一对多关系：一个出版社有多个图书
 *
 * @version 1.0
 */
@TableName("publisher")
public class Publisher implements Serializable {

    @TableId(value = "publisher_id", type = IdType.AUTO)
    private Integer publisherId;  // 出版社编号（主键）
    private String publisherName;     // 出版社名称
    private String city;       // 出版社位置

    // 逻辑删除字段（0=未删除，1=已删除）
    @TableLogic
    private Integer deleted;

    // 一对多关系：一个出版社有多个图书（非数据库列，由联合查询填充）
    @TableField(exist = false)
    private List<Book> books;  // 图书列表

    // 构造方法
    public Publisher() {
    }

    public Publisher(Integer publisherId, String publisherName, String city) {
        this.publisherId = publisherId;
        this.publisherName = publisherName;
        this.city = city;
    }

    // getter 和 setter 方法
    public Integer getPublisherId() {
        return publisherId;
    }

    public void setPublisherId(Integer publisherId) {
        this.publisherId = publisherId;
    }

    public String getPublisherName() {
        return publisherName;
    }

    public void setPublisherName(String publisherName) {
        this.publisherName = publisherName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }

    public List<Book> getBooks() {
        return books;
    }

    public void setBooks(List<Book> books) {
        this.books = books;
    }

    // toString 方法
    @Override
    public String toString() {
        return "出版社{" +
                "出版社编号=" + publisherId +
                ", 出版社名称='" + publisherName + '\'' +
                ", 出版社位置='" + city + '\'' +
                ", 删除状态=" + (deleted == null ? "未设置" : deleted == 0 ? "未删除" : "已删除") +
                '}';
    }
}
