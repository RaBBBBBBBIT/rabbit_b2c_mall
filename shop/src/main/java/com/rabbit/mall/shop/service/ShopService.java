package com.rabbit.mall.shop.service;


import com.rabbit.mall.shop.dao.po.ShopPO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ShopService {
//    public int registerShop(ShopRegisterRequestVo shopRegisterRequestVo);

    List<ShopPO> listShop();
}
