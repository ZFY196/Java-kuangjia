package cn.edu.course.mybatis.util;

import cn.edu.course.mybatis.entity.Emp;
import java.math.BigDecimal;
import java.util.List;

public final class RosterConsole {
    private RosterConsole() {
    }

    public static void show(String caption, List<Emp> roster) {
        BigDecimal payroll = BigDecimal.ZERO;
        System.out.printf("%n%s，共 %d 名员工%n", caption, roster.size());
        int ordinal = 1;
        for (Emp person : roster) {
            System.out.printf("%02d. %s%n", ordinal++, person);
            if (person.getMonthlyPay() != null) {
                payroll = payroll.add(person.getMonthlyPay());
            }
        }
        System.out.printf("本次结果薪资合计：%s 元%n", payroll.toPlainString());
    }
}
