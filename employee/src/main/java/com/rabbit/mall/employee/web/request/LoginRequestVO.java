package com.rabbit.mall.employee.web.request;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class LoginRequestVO {
    @NotBlank
    @Size(max = 64)
    private String username;
    @NotBlank
    @Size(max = 100)
    private String password;
    @NotNull
    private Integer shopId;
}
