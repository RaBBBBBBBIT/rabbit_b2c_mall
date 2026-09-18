package com.rabbit.mall.employee.web;

import com.rabbit.mall.common.response.BaseResponseVO;
import com.rabbit.mall.employee.service.EmployeeService;
import com.rabbit.mall.employee.web.request.AddEmployeeRequestVO;
import com.rabbit.mall.employee.web.request.LoginRequestVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @PostMapping("/save")
    public BaseResponseVO<Integer> save(@RequestBody @Validated AddEmployeeRequestVO req) {
        Integer id = employeeService.addEmployee(req);
        return new BaseResponseVO<>(id);
    }

    @PostMapping("/login")
    public BaseResponseVO<String> login(@RequestBody @Validated LoginRequestVO req) {
        String token = employeeService.login(req);
        return new BaseResponseVO<>(token);
    }

    @PostMapping("/checkToken")
    public BaseResponseVO<Boolean> checkToken(@RequestParam("token") String token) {
        boolean b = employeeService.checkToken(token);
        return new BaseResponseVO<>(b);
    }
}
