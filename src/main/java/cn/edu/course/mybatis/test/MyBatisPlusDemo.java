package cn.edu.course.mybatis.test;

import cn.edu.course.mybatis.entity.Emp;
import cn.edu.course.mybatis.entity.StaffSearch;
import cn.edu.course.mybatis.util.EmpQueryWrappers;
import cn.edu.course.mybatis.util.MyBatisUtil;
import cn.edu.course.mybatis.util.RosterConsole;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public class MyBatisPlusDemo {
    public static void main(String[] arguments) {
        StaffSearch request = StaffSearch.activeStaff();
        if (arguments.length > 0) {
            request.setNameText(arguments[0]);
        }
        if (arguments.length > 1) {
            request.setSex(arguments[1]);
        }
        if (arguments.length > 2) {
            request.setMinimumPay(new java.math.BigDecimal(arguments[2]));
        }
        long pageNumber = arguments.length > 3 ? Long.parseLong(arguments[3]) : 1;
        long pageCapacity = arguments.length > 4 ? Long.parseLong(arguments[4]) : 3;
        if (pageNumber < 1 || pageCapacity < 1) {
            throw new IllegalArgumentException("页码和每页数量必须为正整数");
        }

        MyBatisUtil.inspect(repository -> {
            RosterConsole.show("列名条件构造器", repository.selectList(EmpQueryWrappers.byColumns(request)));
            RosterConsole.show("属性条件构造器", repository.selectList(EmpQueryWrappers.byProperties(request)));
            Page<Emp> slice = repository.selectPage(new Page<>(pageNumber, pageCapacity),
                    EmpQueryWrappers.byProperties(request));
            System.out.printf("%n分页统计：%d 条记录，%d 页；当前第 %d 页，每页 %d 条%n",
                    slice.getTotal(), slice.getPages(), slice.getCurrent(), slice.getSize());
            RosterConsole.show("本页员工", slice.getRecords());
            return null;
        });
        EmpPlusFeaturesDemo.main(new String[0]);
    }
}
