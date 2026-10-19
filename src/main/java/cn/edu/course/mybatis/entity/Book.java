package cn.edu.course.mybatis.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.Version;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@TableName("book")
public class Book implements Serializable {
    @TableId(value = "book_id", type = IdType.AUTO)
    private Integer id;

    @TableField("book_title")
    private String title;

    @TableField("binding")
    private String binding;
    @TableField("publisher")
    private String publisher;
    @TableField("publisher_id")
    private Integer publisherId;
    @TableField(value = "category", updateStrategy = FieldStrategy.NOT_NULL)
    private String category;
    @TableField("price")
    private BigDecimal price;

    @TableField("publish_date")
    private Date publishedOn;

    @TableField("status")
    private Integer saleStatus;

    @TableField(value = "create_time", fill = FieldFill.INSERT, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // 与在售/下架 status 分开，0正常、1逻辑删除。
    @TableLogic(value = "0", delval = "1")
    @TableField("deleted")
    private Integer deleted;

    @Version
    @TableField("version")
    private Integer version;

    // 一对一关系：一本图书对应一份书籍详情（非数据库列，由联合查询填充）
    @TableField(exist = false)
    private BookDetail detail;

    // 多对一关系：多本图书属于同一出版社，保留原 publisher 字符串供单表查询使用。
    @TableField(exist = false)
    private Publisher publisherInfo;

    // 多对多关系：一本图书可由多位作者合著（非数据库列，由联合查询填充）
    @TableField(exist = false)
    private List<Author> authors;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBinding() {
        return binding;
    }

    public void setBinding(String binding) {
        this.binding = binding;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public Integer getPublisherId() {
        return publisherId;
    }

    public void setPublisherId(Integer publisherId) {
        this.publisherId = publisherId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Date getPublishedOn() {
        return publishedOn;
    }

    public void setPublishedOn(Date publishedOn) {
        this.publishedOn = publishedOn;
    }

    public Integer getSaleStatus() {
        return saleStatus;
    }

    public void setSaleStatus(Integer saleStatus) {
        this.saleStatus = saleStatus;
    }

    public BookDetail getDetail() {
        return detail;
    }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    public Integer getDeleted() { return deleted; }
    public void setDeleted(Integer deleted) { this.deleted = deleted; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public void setDetail(BookDetail detail) {
        this.detail = detail;
    }

    public List<Author> getAuthors() {
        return authors;
    }

    public Publisher getPublisherInfo() {
        return publisherInfo;
    }

    public void setPublisherInfo(Publisher publisherInfo) {
        this.publisherInfo = publisherInfo;
    }

    public void setAuthors(List<Author> authors) {
        this.authors = authors;
    }

    public boolean hasDetailChanges() {
        return title != null || publisher != null || publisherId != null || category != null
                || price != null || saleStatus != null;
    }

    @Override
    public String toString() {
        return "图书{" +
                "图书编号=" + id +
                ", 书名='" + title + '\'' +
                ", 装帧='" + binding + '\'' +
                ", 出版社='" + publisher + '\'' +
                ", 分类='" + category + '\'' +
                ", 定价=" + price +
                ", 出版时间=" + (publishedOn == null ? "未填写" : new SimpleDateFormat("yyyy年MM月dd日").format(publishedOn)) +
                ", 状态=" + (saleStatus == null ? "未设置" : saleStatus == 1 ? "在售" : "下架") +
                '}';
    }
}
