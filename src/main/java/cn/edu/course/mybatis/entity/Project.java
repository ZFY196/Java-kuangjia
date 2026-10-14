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
 * 项目实体类（project 表）
 *
 * 与 emp 表是多对多关系：一名员工可参与多个项目，
 * 一个项目也可包含多名员工，通过中间表 emp_project 关联。
 */
@TableName("project")
public class Project implements Serializable {

    @TableId(value = "project_id", type = IdType.AUTO)
    private Integer projectId;     // 项目编号（主键）

    @TableField("project_name")
    private String projectName;    // 项目名称

    @TableField("start_date")
    private Date startDate;        // 项目启动日期

    // 多对多关系：一个项目有多名参与员工（非数据库列）
    @TableField(exist = false)
    private List<Emp> emps;

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public List<Emp> getEmps() {
        return emps;
    }

    public void setEmps(List<Emp> emps) {
        this.emps = emps;
    }

    @Override
    public String toString() {
        return "项目{" +
                "项目编号=" + projectId +
                ", 项目名称='" + projectName + '\'' +
                ", 启动日期=" + (startDate == null ? "未填写" : new SimpleDateFormat("yyyy年MM月dd日").format(startDate)) +
                '}';
    }
}
