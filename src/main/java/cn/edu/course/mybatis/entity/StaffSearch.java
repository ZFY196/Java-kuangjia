package cn.edu.course.mybatis.entity;

import java.math.BigDecimal;

/** 查询条件独立于持久化实体；空白文字统一按未指定处理。 */
public final class StaffSearch {
    private String nameText;
    private String sex;
    private String department;
    private Integer activeFlag;
    private BigDecimal minimumPay;

    public static StaffSearch activeStaff() {
        return new StaffSearch().setActiveFlag(1);
    }

    public String getNameText() {
        return nameText;
    }

    public StaffSearch setNameText(String text) {
        nameText = clean(text);
        return this;
    }

    public String getSex() {
        return sex;
    }

    public StaffSearch setSex(String value) {
        sex = clean(value);
        return this;
    }

    public String getDepartment() {
        return department;
    }

    public StaffSearch setDepartment(String value) {
        department = clean(value);
        return this;
    }

    public Integer getActiveFlag() {
        return activeFlag;
    }

    public StaffSearch setActiveFlag(Integer value) {
        activeFlag = value;
        return this;
    }

    public BigDecimal getMinimumPay() {
        return minimumPay;
    }

    public StaffSearch setMinimumPay(BigDecimal value) {
        if (value != null && value.signum() < 0) {
            throw new IllegalArgumentException("最低薪资不能为负数");
        }
        minimumPay = value;
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
