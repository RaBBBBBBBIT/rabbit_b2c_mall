package com.rabbit.mall.shop.web.request;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@Data
public class ShopRegisterRequestVO {
    @NotBlank(message = "店铺名不能为空")
    @Length(max = 6, message = "店铺名过长")
    private String shopName;

    @NotBlank(message = "账户不能为空")
    private String adminAccount;

    @NotBlank(message = "账户密码不能为空")
    private String adminPassword;
}
