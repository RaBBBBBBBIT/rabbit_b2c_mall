package com.rabbit.mall.shop.service.event.handler;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.rabbit.mall.shop.service.MessageService;
import com.rabbit.mall.shop.service.event.ShopRegisterEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InitShopMessageEventHandle implements InitializingBean {

    @Autowired
    private EventBus eventBus;
    @Autowired
    private MessageService messageService;

    @Subscribe
    public void handler(ShopRegisterEvent event) {
        log.info("收到注册成功事件：{}, 插入欢迎消息", event);
        messageService.saveWelcomeMessage(event.getShopId(), "欢迎注册本系统", "系统规范，系统规约");
    }
    @Override
    public void afterPropertiesSet() throws Exception {
        eventBus.register(this);
    }
}
