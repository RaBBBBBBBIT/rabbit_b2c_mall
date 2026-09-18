package com.rabbit.mall.shop.service.impl;

import cn.hutool.core.lang.Assert;
import com.rabbit.mall.shop.dao.mapper.MessageMapper;
import com.rabbit.mall.shop.dao.po.MessagePO;
import com.rabbit.mall.shop.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageServiceImpl implements MessageService {
    @Autowired
    private MessageMapper messageMapper;

    @Override
    public void saveWelcomeMessage(Integer shopId, String title, String content) {
        Assert.notNull(shopId);
        Assert.notNull(content);
        Assert.notNull(title);

        MessagePO po = new MessagePO();
        po.setShopId(shopId);
        po.setTitle(title);
        po.setContent(content);
        po.setMsgType(1);
        po.setIsRead(0);
        Integer row = messageMapper.saveMessage(po);
        Assert.equals(row, 1, "保存欢迎消息异常");
    }
}
