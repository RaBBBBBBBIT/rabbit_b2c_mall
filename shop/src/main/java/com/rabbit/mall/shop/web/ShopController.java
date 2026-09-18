package com.rabbit.mall.shop.web;

import com.rabbit.mall.shop.dao.po.ShopPO;
import com.rabbit.mall.shop.service.ShopService;
import com.rabbit.mall.shop.web.request.ShopRegisterRequestVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/shop")
public class ShopController {

    @Autowired
    ShopService shopService;

    @GetMapping("/hello")
    public String hello() {
        return "hello world";
    }

    @PostMapping("/register")
    public String registerShop(ShopRegisterRequestVo shopRegisterRequestVo) {
//        shopService.registerShop(shopRegisterRequestVo);
        return "success";
    }

    @GetMapping("/list")
    public List<ShopPO> list() {
        return shopService.listShop();
    }

}
