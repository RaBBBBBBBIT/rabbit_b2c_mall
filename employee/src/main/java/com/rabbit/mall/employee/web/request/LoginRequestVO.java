package com.rabbit.mall.employee.web.request;

import lombok.Data;

@Data
public class LoginRequestVO {
    private String username;
    private String password;
    private Integer shopId;
}
