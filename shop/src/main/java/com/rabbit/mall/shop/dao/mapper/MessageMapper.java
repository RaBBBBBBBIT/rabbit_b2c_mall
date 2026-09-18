package com.rabbit.mall.shop.dao.mapper;

import com.rabbit.mall.shop.dao.po.MessagePO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MessageMapper {
    Integer saveMessage(MessagePO messagePO);
}
