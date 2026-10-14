package cn.edu.course.mybatis.util;

import cn.edu.course.mybatis.entity.Emp;
import cn.edu.course.mybatis.entity.StaffSearch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.math.BigDecimal;

public final class EmpQueryWrappers {
    private EmpQueryWrappers() {
    }

    public static QueryWrapper<Emp> byColumns(StaffSearch request) {
        StaffSearch options = request == null ? StaffSearch.activeStaff() : request;
        QueryWrapper<Emp> result = new QueryWrapper<>();
        result.orderByDesc("salary").orderByAsc("emp_id");
        if (options.getDepartment() != null) {
            result.eq("dept", options.getDepartment());
        }
        if (options.getActiveFlag() != null) {
            result.eq("status", options.getActiveFlag());
        }
        if (options.getMinimumPay() != null) {
            result.ge("salary", options.getMinimumPay());
        }
        if (options.getNameText() != null) {
            result.like("emp_name", options.getNameText());
        }
        if (options.getSex() != null) {
            result.eq("gender", options.getSex());
        }
        return result;
    }

    public static LambdaQueryWrapper<Emp> byProperties(StaffSearch request) {
        StaffSearch options = request == null ? StaffSearch.activeStaff() : request;
        LambdaQueryWrapper<Emp> result = new LambdaQueryWrapper<>();
        result.orderByDesc(Emp::getMonthlyPay).orderByAsc(Emp::getId);
        if (options.getSex() != null) {
            result.eq(Emp::getSex, options.getSex());
        }
        if (options.getNameText() != null) {
            result.like(Emp::getName, options.getNameText());
        }
        if (options.getMinimumPay() != null) {
            result.ge(Emp::getMonthlyPay, options.getMinimumPay());
        }
        if (options.getActiveFlag() != null) {
            result.eq(Emp::getActiveFlag, options.getActiveFlag());
        }
        if (options.getDepartment() != null) {
            result.eq(Emp::getDepartment, options.getDepartment());
        }
        return result;
    }

    public static QueryWrapper<Emp> byColumns(String name, String sex, BigDecimal minimum) {
        return byColumns(StaffSearch.activeStaff().setNameText(name).setSex(sex).setMinimumPay(minimum));
    }

    public static LambdaQueryWrapper<Emp> byProperties(String name, String sex, BigDecimal minimum) {
        return byProperties(StaffSearch.activeStaff().setNameText(name).setSex(sex).setMinimumPay(minimum));
    }
}
