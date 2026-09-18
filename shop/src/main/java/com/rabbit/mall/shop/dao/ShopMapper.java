package com.rabbit.mall.shop.dao;

import com.rabbit.mall.shop.dao.po.ShopPO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ShopMapper {
    List<ShopPO> selectList();
}
