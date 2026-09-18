package com.rabbit.mall.employee.service;

import com.rabbit.mall.employee.web.request.AddEmployeeRequestVO;
import com.rabbit.mall.employee.web.request.LoginRequestVO;

public interface EmployeeService {
    Integer addEmployee(AddEmployeeRequestVO vo);
    String login(LoginRequestVO req);
    boolean checkToken(String token);
}
