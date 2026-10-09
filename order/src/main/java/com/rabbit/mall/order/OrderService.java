package com.rabbit.mall.order;

import com.rabbit.mall.common.auth.AuthenticatedUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.*;

@Service
public class OrderService {
    private final OrderRepository orders;
    private final RestTemplate http;
    @Value("${product.service-url}") private String productServiceUrl;
    @Value("${mall.internal.token}") private String internalToken;

    public OrderService(OrderRepository orders, RestTemplate http) { this.orders=orders; this.http=http; }

    @Transactional
    public OrderRepository.OrderRecord create(AuthenticatedUser user, String authorization, CreateOrderRequest request) {
        if (request.items == null || request.items.isEmpty() || request.items.size() > 50)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"order must contain 1 to 50 lines");
        String orderNo="M"+UUID.randomUUID().toString().replace("-","").substring(0,20).toUpperCase(Locale.ROOT);
        List<OrderRepository.OrderItem> items=new ArrayList<>();
        List<Line> reserved=new ArrayList<>();
        BigDecimal total=BigDecimal.ZERO;
        try {
            for (CreateOrderRequest.Item requested:request.items) {
                if (requested.productId==null || requested.quantity==null || requested.quantity<1 || requested.quantity>999)
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"invalid product or quantity");
                ProductSnapshot product=getProduct(requested.productId,authorization);
                if (product.status==null || product.status!=1) throw new ResponseStatusException(HttpStatus.CONFLICT,"product is not on sale");
                reserve(product.id,requested.quantity);
                reserved.add(new Line(product.id,requested.quantity));
                OrderRepository.OrderItem item=new OrderRepository.OrderItem(); item.productId=product.id;
                item.productName=product.name; item.unitPrice=product.price; item.quantity=requested.quantity;
                item.subtotal=product.price.multiply(BigDecimal.valueOf(requested.quantity));
                total=total.add(item.subtotal); items.add(item);
            }
            OrderRepository.OrderRecord order=orders.create(user.getShopId(),user.getEmployeeId(),orderNo,total);
            for (OrderRepository.OrderItem item:items) orders.addItem(order.id,item);
            orders.addLog(order.id,null,"WAIT_PAY",user.getEmployeeId(),"order created; stock reserved");
            return order;
        } catch (RuntimeException ex) {
            releaseAll(reserved);
            throw ex;
        }
    }

    public List<OrderRepository.OrderRecord> list(Integer shopId) { return orders.list(shopId); }
    public Map<String,Object> detail(Integer shopId,Integer id) {
        OrderRepository.OrderRecord order=owned(shopId,id); Map<String,Object> result=new LinkedHashMap<>();
        result.put("order",order); result.put("items",orders.items(id)); return result;
    }

    @Transactional
    public OrderRepository.OrderRecord transition(AuthenticatedUser user,Integer id,String action) {
        OrderRepository.OrderRecord order=owned(user.getShopId(),id);
        String next;
        if ("pay".equals(action) && "WAIT_PAY".equals(order.status)) next="PAID";
        else if ("ship".equals(action) && "PAID".equals(order.status)) next="SHIPPED";
        else if ("confirm".equals(action) && "SHIPPED".equals(order.status)) next="COMPLETED";
        else if ("cancel".equals(action) && "WAIT_PAY".equals(order.status)) next="CANCELLED";
        else throw new ResponseStatusException(HttpStatus.CONFLICT,"action is invalid for order status " + order.status);
        if (orders.transition(id,order.status,next)!=1) throw new ResponseStatusException(HttpStatus.CONFLICT,"order status changed; retry");
        if ("cancel".equals(action)) {
            for (OrderRepository.OrderItem item:orders.items(id)) release(item.productId,item.quantity);
        }
        orders.addLog(id,order.status,next,user.getEmployeeId(),"order "+action);
        return owned(user.getShopId(),id);
    }

    private OrderRepository.OrderRecord owned(Integer shopId,Integer id) {
        return orders.findByIdAndShop(id,shopId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"order not found"));
    }
    private ProductSnapshot getProduct(Integer id,String auth) {
        HttpHeaders headers=new HttpHeaders(); headers.set(HttpHeaders.AUTHORIZATION,auth);
        try {
            ResponseEntity<BaseResponseVO> response=http.exchange(productServiceUrl+"/product/"+id,HttpMethod.GET,new HttpEntity<>(headers),BaseResponseVO.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody()==null || response.getBody().getData()==null)
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,"product lookup failed");
            Map data=(Map)response.getBody().getData(); ProductSnapshot p=new ProductSnapshot();
            p.id=((Number)data.get("id")).intValue(); p.name=(String)data.get("name"); p.status=((Number)data.get("status")).intValue();
            p.price=new BigDecimal(data.get("price").toString()); return p;
        } catch (ResponseStatusException ex) { throw ex; }
        catch (RuntimeException ex) { throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,"product service unavailable",ex); }
    }
    private void reserve(Integer id,int qty) { internalStockCall(id,qty,"reserve"); }
    private void release(Integer id,int qty) { internalStockCall(id,qty,"release"); }
    private void internalStockCall(Integer id,int qty,String action) {
        HttpHeaders headers=new HttpHeaders(); headers.set("X-Internal-Token",internalToken);
        try { http.exchange(productServiceUrl+"/product/internal/"+id+"/"+action+"?quantity="+qty,HttpMethod.POST,new HttpEntity<>(headers),BaseResponseVO.class); }
        catch (HttpStatusCodeException ex) {
            if (ex.getStatusCode() == HttpStatus.CONFLICT)
                throw new ResponseStatusException(HttpStatus.CONFLICT,"product unavailable or insufficient stock",ex);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,"product inventory update failed",ex);
        }
        catch (RuntimeException ex) { throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,"product inventory update failed",ex); }
    }
    private void releaseAll(List<Line> lines) {
        for(int i=lines.size()-1;i>=0;i--) { Line l=lines.get(i); try { release(l.id,l.quantity); } catch(RuntimeException ignored) { /* preserve the original failure */ } }
    }
    private static class Line { Integer id; int quantity; Line(Integer id,int quantity){this.id=id;this.quantity=quantity;} }
    private static class ProductSnapshot { Integer id,status; String name; BigDecimal price; }
    public static class CreateOrderRequest { public List<Item> items; public static class Item { public Integer productId,quantity; } }
    public static class BaseResponseVO<T> { private int status; private String message; private T data; public int getStatus(){return status;} public String getMessage(){return message;} public T getData(){return data;} }
}
