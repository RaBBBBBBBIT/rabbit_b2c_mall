package com.rabbit.mall.shop.service.impl;

import com.rabbit.mall.shop.dao.mapper.ShopMapper;
import com.rabbit.mall.shop.dao.po.ShopPO;
import com.rabbit.mall.shop.service.ShopService;
import com.rabbit.mall.shop.web.request.ShopRegisterRequestVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DuplicateKeyException;

import java.util.List;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class ShopServiceImpl implements ShopService {
    @Autowired
    private ShopMapper shopMapper;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    @Transactional
    public Integer register(ShopRegisterRequestVO vo) {
        int count = shopMapper.exist(vo.getShopName());
        if (count != 0) throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.CONFLICT, "shop name already exists");

        ShopPO shopPO = new ShopPO();
        BeanUtils.copyProperties(vo, shopPO);
        shopPO.setAdminPassword(passwordEncoder.encode(vo.getAdminPassword()));
        shopPO.setStatus(1);

        int row;
        try { row = shopMapper.registerShop(shopPO); }
        catch (DuplicateKeyException ex) { throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.CONFLICT, "shop name already exists"); }
        if (row != 1) throw new IllegalStateException("failed to save shop");
        return shopPO.getId();
    }

//    @Override
//    public int registerShop(ShopRegisterRequestVo shopRegisterRequestVo) {
//        return
//    }
}
