package com.rabbit.mall.employee.web;

import com.rabbit.mall.common.response.BaseResponseVO;
import com.rabbit.mall.employee.service.EmployeeService;
import com.rabbit.mall.employee.web.request.AddEmployeeRequestVO;
import com.rabbit.mall.employee.web.request.LoginRequestVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @Value("${mall.internal.token}")
    private String internalToken;

    @PostMapping("/internal/initialize")
    public BaseResponseVO<Integer> save(@RequestHeader(value = "X-Internal-Token", defaultValue = "") String token,
                                        @RequestBody @Validated AddEmployeeRequestVO req) {
        if (!MessageDigest.isEqual(internalToken.getBytes(StandardCharsets.UTF_8), token.getBytes(StandardCharsets.UTF_8))) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "forbidden");
        }
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

    @PostMapping("/internal/validate")
    public BaseResponseVO<Boolean> validate(@RequestHeader(value = "X-Internal-Token", defaultValue = "") String token,
                                            @RequestHeader(value = "Authorization", defaultValue = "") String authorization) {
        if (!MessageDigest.isEqual(internalToken.getBytes(StandardCharsets.UTF_8), token.getBytes(StandardCharsets.UTF_8))) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "forbidden");
        }
        String raw = authorization.startsWith("Bearer ") ? authorization.substring(7) : authorization;
        return new BaseResponseVO<>(employeeService.checkToken(raw));
    }

    @PostMapping("/logout")
    public BaseResponseVO<Boolean> logout(@RequestHeader(value = "Authorization", defaultValue = "") String token) {
        String raw = token.startsWith("Bearer ") ? token.substring(7) : token;
        return new BaseResponseVO<>(employeeService.logout(raw));
    }
}
