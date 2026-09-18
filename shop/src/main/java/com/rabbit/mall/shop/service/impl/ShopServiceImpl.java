package com.rabbit.mall.shop.service.impl;

import cn.hutool.core.lang.Assert;
import com.rabbit.mall.shop.dao.mapper.ShopMapper;
import com.rabbit.mall.shop.dao.po.ShopPO;
import com.rabbit.mall.shop.service.ShopService;
import com.rabbit.mall.shop.web.request.ShopRegisterRequestVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShopServiceImpl implements ShopService {
    @Autowired
    private ShopMapper shopMapper;

    @Override
    public Integer register(ShopRegisterRequestVO vo) {
        int count = shopMapper.exist(vo.getShopName());
        Assert.equals(count, 0, "店铺名已存在");

        ShopPO shopPO = new ShopPO();
        BeanUtils.copyProperties(vo, shopPO);
        shopPO.setStatus(1);

        Assert.notBlank(shopPO.getShopName());
        Assert.notBlank(shopPO.getAdminAccount());
        Assert.notBlank(shopPO.getAdminPassword());

        int row = shopMapper.registerShop(shopPO);
        Assert.equals(row, 1, "保存到数据库异常");
        return shopPO.getId();
    }

//    @Override
//    public int registerShop(ShopRegisterRequestVo shopRegisterRequestVo) {
//        return
//    }
}
