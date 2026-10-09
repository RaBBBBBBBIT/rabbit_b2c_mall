package com.rabbit.mall.shop.web.request;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class ShopRegisterRequestVO {
    @NotBlank(message = "店铺名不能为空")
    @Length(max = 6, message = "店铺名过长")
    private String shopName;

    @NotBlank(message = "账户不能为空")
    @Size(max = 64, message = "账户名过长")
    private String adminAccount;

    @NotBlank(message = "账户密码不能为空")
    @Size(min = 8, max = 72, message = "密码长度必须为8到72个字符")
    private String adminPassword;
}
