package cn.edu.course.mybatis.test;

import cn.edu.course.mybatis.entity.Emp;
import cn.edu.course.mybatis.mapper.EmpMapper;
import cn.edu.course.mybatis.util.MyBatisUtil;
import cn.edu.course.mybatis.util.RosterConsole;
import java.util.Collections;
import java.util.List;

public class MyBatisDemo {
    public static void main(String[] arguments) {
        List<Emp> roster = MyBatisUtil.inspectClassic(EmpMapper::listRoster);
        RosterConsole.show("普通 MyBatis 员工名册", roster);
        if (roster.isEmpty()) {
            return;
        }
        Integer requestedKey = arguments.length == 0 ? roster.get(0).getId()
                : Integer.valueOf(arguments[0]);
        Emp found = MyBatisUtil.inspectClassic(repository -> repository.findRecord(requestedKey));
        RosterConsole.show("编号 " + requestedKey + " 的查询结果",
                found == null ? Collections.emptyList() : Collections.singletonList(found));
        MultiTableRelationDemo.main(arguments);
    }
}
