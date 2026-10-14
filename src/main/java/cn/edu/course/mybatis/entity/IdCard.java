package cn.edu.course.mybatis.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 身份证实体类（idcard 表）
 *
 * 与 emp 表是一对一关系：一名员工只有一张证件（idcard.emp_id 唯一约束）
 */
@TableName("idcard")
public class IdCard implements Serializable {

    @TableId(value = "idcard_id", type = IdType.AUTO)
    private Integer idcardId;      // 证件编号（主键）

    @TableField("emp_id")
    private Integer empId;         // 员工编号（唯一外键）

    @TableField("card_no")
    private String cardNo;         // 身份证号

    @TableField("expire_date")
    private Date expireDate;       // 证件有效期至

    public Integer getIdcardId() {
        return idcardId;
    }

    public void setIdcardId(Integer idcardId) {
        this.idcardId = idcardId;
    }

    public Integer getEmpId() {
        return empId;
    }

    public void setEmpId(Integer empId) {
        this.empId = empId;
    }

    public String getCardNo() {
        return cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }

    public Date getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(Date expireDate) {
        this.expireDate = expireDate;
    }

    @Override
    public String toString() {
        return "身份证{" +
                "证件编号=" + idcardId +
                ", 员工编号=" + empId +
                ", 身份证号='" + cardNo + '\'' +
                ", 有效期至=" + (expireDate == null ? "未填写" : new SimpleDateFormat("yyyy年MM月dd日").format(expireDate)) +
                '}';
    }
}
