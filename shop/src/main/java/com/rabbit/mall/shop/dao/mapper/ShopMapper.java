package com.rabbit.mall.shop.dao.mapper;

import com.rabbit.mall.shop.dao.po.ShopPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ShopMapper {
    Integer registerShop(ShopPO shopPO);

    int exist(@Param("shopName") String shopName);
}
