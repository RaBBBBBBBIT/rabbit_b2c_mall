package com.rabbit.mall.shop.service.event.handler;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.rabbit.mall.shop.service.event.ShopRegisterEvent;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@Component
public class InitEmployeeEventHandle implements InitializingBean {
    private final EventBus eventBus;
    private final RestTemplate http;
    @Value("${employee.service-url}") private String employeeServiceUrl;
    @Value("${mall.internal.token}") private String internalToken;

    public InitEmployeeEventHandle(EventBus eventBus, RestTemplate http) { this.eventBus=eventBus; this.http=http; }

    @Subscribe
    public void initialize(ShopRegisterEvent event) {
        HttpHeaders headers=new HttpHeaders(); headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Internal-Token",internalToken);
        Map<String,Object> body=new HashMap<>(); body.put("shopId",event.getShopId());
        body.put("username",event.getAdminAccount()); body.put("password",event.getAdminPwd());
        http.exchange(employeeServiceUrl+"/employee/internal/initialize",HttpMethod.POST,new HttpEntity<>(body,headers),String.class);
    }

    @Override public void afterPropertiesSet() { eventBus.register(this); }
}
