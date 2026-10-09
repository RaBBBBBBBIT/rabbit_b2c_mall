package com.rabbit.mall.order;

import com.rabbit.mall.common.auth.AuthenticatedUser;
import com.rabbit.mall.common.response.BaseResponseVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/order")
public class OrderController {
    private final OrderService service;
    private final RestTemplate http;
    @Value("${jwt.token.key}") private String jwtKey;
    @Value("${mall.internal.token}") private String internalToken;
    @Value("${employee.service-url}") private String employeeServiceUrl;
    public OrderController(OrderService service, RestTemplate http) { this.service=service; this.http=http; }

    @PostMapping("/create")
    public BaseResponseVO<OrderRepository.OrderRecord> create(@RequestHeader("Authorization") String auth,
                                                               @RequestBody OrderService.CreateOrderRequest request) {
        AuthenticatedUser user=user(auth); return new BaseResponseVO<>(service.create(user,auth,request));
    }
    @GetMapping("/list")
    public BaseResponseVO<List<OrderRepository.OrderRecord>> list(@RequestHeader("Authorization") String auth) {
        return new BaseResponseVO<>(service.list(user(auth).getShopId()));
    }
    @GetMapping("/{id}")
    public BaseResponseVO<Map<String,Object>> detail(@RequestHeader("Authorization") String auth,@PathVariable Integer id) {
        return new BaseResponseVO<>(service.detail(user(auth).getShopId(),id));
    }
    @PostMapping("/{id}/{action}")
    public BaseResponseVO<OrderRepository.OrderRecord> transition(@RequestHeader("Authorization") String auth,
                                                                   @PathVariable Integer id,@PathVariable String action) {
        if (!action.equals("pay") && !action.equals("ship") && !action.equals("confirm") && !action.equals("cancel"))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"unknown order action");
        return new BaseResponseVO<>(service.transition(user(auth),id,action));
    }
    private AuthenticatedUser user(String authorization) {
        AuthenticatedUser user;
        try { user = AuthenticatedUser.from(authorization,jwtKey); }
        catch(RuntimeException ex) { throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"invalid or expired token"); }
        HttpHeaders headers = new HttpHeaders(); headers.set(HttpHeaders.AUTHORIZATION, authorization); headers.set("X-Internal-Token", internalToken);
        try {
            ResponseEntity<Map> response = http.exchange(employeeServiceUrl + "/employee/internal/validate", HttpMethod.POST, new HttpEntity<>(headers), Map.class);
            if (response.getBody() == null || !Boolean.TRUE.equals(response.getBody().get("data")))
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"invalid or expired token");
            return user;
        } catch (ResponseStatusException ex) { throw ex; }
        catch (RuntimeException ex) { throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"authentication service unavailable",ex); }
    }
}
