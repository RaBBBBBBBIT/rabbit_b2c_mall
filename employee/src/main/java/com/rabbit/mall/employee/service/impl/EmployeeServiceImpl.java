package com.rabbit.mall.employee.service.impl;


import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import com.rabbit.mall.employee.dao.mapper.EmployeeMapper;
import com.rabbit.mall.employee.dao.po.EmployeePO;
import com.rabbit.mall.employee.service.EmployeeService;
import com.rabbit.mall.employee.web.request.AddEmployeeRequestVO;
import com.rabbit.mall.employee.web.request.LoginRequestVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class EmployeeServiceImpl implements EmployeeService {
    @Autowired
    private EmployeeMapper employeeMapper;

    @Value("${jwt.token.key}")
    private String tokenKey;

    @Value("${jwt.token.ttl-seconds:86400}")
    private long tokenTtlSeconds;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Value("${employee.default-avatar-url:}")
    private String defaultAvatarUrl;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public Integer addEmployee(AddEmployeeRequestVO vo) {
        EmployeePO existing = employeeMapper.getEmployee(vo.getShopId(), vo.getUsername());
        if (existing != null) {
            return existing.getId(); // shop-registration event is safe to deliver more than once
        }
        EmployeePO po = new EmployeePO();
        BeanUtils.copyProperties(vo, po);
        po.setPassword(passwordEncoder.encode(vo.getPassword()));
        po.setAvatarUrl(defaultAvatarUrl);
        po.setStatus(1);

        Integer row = employeeMapper.save(po);
        if (row == null || row != 1) throw new IllegalStateException("保存员工信息失败");
        return po.getId();
    }

    @Override
    public String login(LoginRequestVO req) {
        EmployeePO employeePO = employeeMapper.getEmployee(req.getShopId(), req.getUsername());
        if (Objects.isNull(employeePO)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid username or password");
        }
        if (employeePO.getStatus() == null || employeePO.getStatus() != 1
                || !passwordEncoder.matches(req.getPassword(), employeePO.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid username or password");
        }

        Map<String, Object> payload = new HashMap<>(4);
        payload.put("id",  employeePO.getId());
        payload.put("username", employeePO.getUsername());
        payload.put("shopId", employeePO.getShopId());
        payload.put("exp", System.currentTimeMillis() / 1000L + tokenTtlSeconds);

        if (employeeMapper.updateLoginInfo(employeePO.getId()) != 1) {
            throw new IllegalStateException("员工账号已停用");
        }

        String token = JWTUtil.createToken(payload, tokenKey.getBytes(StandardCharsets.UTF_8));
        redisTemplate.opsForValue().set(getTokenKey(employeePO.getShopId(), employeePO.getId()), token,
                tokenTtlSeconds, TimeUnit.SECONDS);
        return token;
    }

    private String getTokenKey(Integer shopId, Integer userId) {
        return String.format("token:%1$s:%2$s", shopId, userId);
    }

    @Override
    public boolean checkToken(String token) {
        try {
            if (!JWTUtil.verify(token, tokenKey.getBytes(StandardCharsets.UTF_8))) return false;
            JWT payload = JWTUtil.parseToken(token);
            if (payload == null) return false;
            Object exp = payload.getPayload("exp");
            Object userId = payload.getPayload("id");
            Object shopId = payload.getPayload("shopId");
            if (exp == null || userId == null || shopId == null
                    || Long.parseLong(exp.toString()) <= System.currentTimeMillis() / 1000L) return false;
            String activeToken = redisTemplate.opsForValue().get(
                    getTokenKey(Integer.valueOf(shopId.toString()), Integer.valueOf(userId.toString())));
            return Objects.equals(token, activeToken);
        } catch (RuntimeException invalidToken) { return false; }
    }

    @Override
    public boolean logout(String token) {
        try {
            if (!JWTUtil.verify(token, tokenKey.getBytes(StandardCharsets.UTF_8))) return false;
            JWT payload = JWTUtil.parseToken(token);
            if (payload == null || payload.getPayload("shopId") == null || payload.getPayload("id") == null) return false;
            redisTemplate.delete(getTokenKey(Integer.valueOf(payload.getPayload("shopId").toString()),
                    Integer.valueOf(payload.getPayload("id").toString())));
            return true;
        } catch (RuntimeException invalidToken) { return false; }
    }
}
