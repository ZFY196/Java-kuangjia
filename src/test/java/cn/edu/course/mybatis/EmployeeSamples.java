package cn.edu.course.mybatis;

import cn.edu.course.mybatis.domain.Employee;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

final class EmployeeSamples {
    private EmployeeSamples() {
    }

    static Employee newSupportEngineer() {
        Employee employee = new Employee();
        employee.setName("沈嘉禾");
        employee.setGender("男");
        employee.setDepartment("技术支持部");
        employee.setJobTitle("交付工程师");
        employee.setSalary(new BigDecimal("10350.00"));
        employee.setHireDate(dateOf(2025, Calendar.JUNE, 9));
        employee.setActive(1);
        return employee;
    }

    static Date dateOf(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(year, month, day);
        return calendar.getTime();
    }
}
