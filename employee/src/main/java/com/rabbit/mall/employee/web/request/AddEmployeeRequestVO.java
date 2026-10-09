package com.rabbit.mall.employee.web.request;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class AddEmployeeRequestVO {
    @NotNull
    private Integer shopId;
    @NotBlank
    @Size(max = 64)
    private String username;
    @NotBlank
    @Size(min = 8, max = 100)
    private String password;
}
