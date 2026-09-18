package com.rabbit.mall.shop.web;

import com.google.common.eventbus.EventBus;
import com.rabbit.mall.common.response.BaseResponseVO;
import com.rabbit.mall.shop.dao.po.ShopPO;
import com.rabbit.mall.shop.service.ShopService;
import com.rabbit.mall.shop.service.event.ShopRegisterEvent;
import com.rabbit.mall.shop.web.request.ShopRegisterRequestVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@RestController
@RequestMapping("/shop")
public class ShopController {

    @Autowired
    ShopService shopService;

    @Autowired
    EventBus eventBus;

    @GetMapping("/hello")
    public String hello() {
        return "hello world";
    }

    @PostMapping("/register")
    public BaseResponseVO<?> register(@RequestBody @Validated ShopRegisterRequestVO req) {
        Integer id = shopService.register(req);

        ShopRegisterEvent event = new ShopRegisterEvent();
        event.setShopId(id);
        event.setShopName(req.getShopName());
        event.setAdminAccount(req.getAdminAccount());
        event.setAdminPwd(req.getAdminPassword());
        eventBus.post(event);

        return new BaseResponseVO<>();
    }


}
