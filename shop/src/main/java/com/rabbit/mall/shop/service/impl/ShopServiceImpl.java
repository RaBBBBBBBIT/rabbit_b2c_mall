package com.rabbit.mall.shop.service.impl;

import com.rabbit.mall.shop.dao.ShopMapper;
import com.rabbit.mall.shop.dao.po.ShopPO;
import com.rabbit.mall.shop.service.ShopService;
import com.rabbit.mall.shop.web.request.ShopRegisterRequestVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class ShopServiceImpl implements ShopService {
    @Autowired
    private ShopMapper shopMapper;

    @Override
    public List<ShopPO> listShop() {
        return shopMapper.selectList();
    }

//    @Override
//    public int registerShop(ShopRegisterRequestVo shopRegisterRequestVo) {
//        return
//    }
}
