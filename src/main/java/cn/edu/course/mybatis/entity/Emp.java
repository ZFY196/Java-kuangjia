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

@TableName("emp")
public class Emp implements Serializable {
    @TableId(value = "emp_id", type = IdType.AUTO)
    private Integer id;

    @TableField("emp_name")
    private String name;

    @TableField("gender")
    private String sex;
    @TableField("dept")
    private String department;
    @TableField(value = "post", updateStrategy = FieldStrategy.NOT_NULL)
    private String position;
    @TableField("salary")
    private BigDecimal monthlyPay;

    @TableField("hire_date")
    private Date joinedOn;

    @TableField("status")
    private Integer activeFlag;

    @TableField(value = "create_time", fill = FieldFill.INSERT, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // 与在职/离职 status 分开，0正常、1逻辑删除。
    @TableLogic(value = "0", delval = "1")
    @TableField("deleted")
    private Integer deleted;

    @Version
    @TableField("version")
    private Integer version;

    // 一对一关系：一名员工对应一张身份证（非数据库列，由联合查询填充）
    @TableField(exist = false)
    private IdCard idCard;

    // 多对一关系：多名员工属于同一部门，保留原 department 字符串供单表查询使用。
    @TableField(exist = false)
    private Dept deptInfo;

    // 多对多关系：一名员工可参与多个项目（非数据库列，由联合查询填充）
    @TableField(exist = false)
    private List<Project> projects;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public BigDecimal getMonthlyPay() {
        return monthlyPay;
    }

    public void setMonthlyPay(BigDecimal monthlyPay) {
        this.monthlyPay = monthlyPay;
    }

    public Date getJoinedOn() {
        return joinedOn;
    }

    public void setJoinedOn(Date joinedOn) {
        this.joinedOn = joinedOn;
    }

    public Integer getActiveFlag() {
        return activeFlag;
    }

    public void setActiveFlag(Integer activeFlag) {
        this.activeFlag = activeFlag;
    }

    public IdCard getIdCard() {
        return idCard;
    }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    public Integer getDeleted() { return deleted; }
    public void setDeleted(Integer deleted) { this.deleted = deleted; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public void setIdCard(IdCard idCard) {
        this.idCard = idCard;
    }

    public List<Project> getProjects() {
        return projects;
    }

    public Dept getDeptInfo() {
        return deptInfo;
    }

    public void setDeptInfo(Dept deptInfo) {
        this.deptInfo = deptInfo;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }

    public boolean hasProfileChanges() {
        return name != null || department != null || position != null
                || monthlyPay != null || activeFlag != null;
    }

    @Override
    public String toString() {
        return "员工{" +
                "员工编号=" + id +
                ", 姓名='" + name + '\'' +
                ", 性别='" + sex + '\'' +
                ", 部门='" + department + '\'' +
                ", 岗位='" + position + '\'' +
                ", 薪资=" + monthlyPay +
                ", 入职时间=" + (joinedOn == null ? "未填写" : new SimpleDateFormat("yyyy年MM月dd日").format(joinedOn)) +
                ", 状态=" + (activeFlag == null ? "未设置" : activeFlag == 1 ? "在职" : "离职") +
                '}';
    }
}
