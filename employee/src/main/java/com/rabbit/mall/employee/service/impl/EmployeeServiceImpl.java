package com.rabbit.mall.employee.service.impl;


import cn.hutool.core.lang.Assert;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.rabbit.mall.employee.dao.mapper.EmployeeMapper;
import com.rabbit.mall.employee.dao.po.EmployeePO;
import com.rabbit.mall.employee.service.EmployeeService;
import com.rabbit.mall.employee.web.request.AddEmployeeRequestVO;
import com.rabbit.mall.employee.web.request.LoginRequestVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

@Service
public class EmployeeServiceImpl implements EmployeeService {
    @Autowired
    private EmployeeMapper employeeMapper;

    @Value("{jwt.token.key:rabbit}")
    private String tokenKey;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Value("${employee.default-avatar-url:}")
    private String defaultAvatarUrl;

    @Override
    public Integer addEmployee(AddEmployeeRequestVO vo) {
        Assert.notBlank(vo.getUsername());
        Assert.notNull(vo.getShopId());
        Assert.notBlank(vo.getUsername());

        EmployeePO po = new EmployeePO();
        BeanUtils.copyProperties(vo, po);
        po.setAvatarUrl(defaultAvatarUrl);
        po.setStatus(1);

        Integer row = employeeMapper.save(po);
        Assert.equals(row, 1, "保存员工信息失败");
        return po.getId();
    }

    @Override
    public String login(LoginRequestVO req) {
        EmployeePO employeePO = employeeMapper.getEmployee(req.getShopId(), req.getUsername());
        if (Objects.isNull(employeePO)) {
            throw new RuntimeException("账户不存在");
        }
        if (!Objects.equals(employeePO.getPassword(), req.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        Map<String, Object> payload = Maps.newHashMapWithExpectedSize(3);
        payload.put("id",  employeePO.getId());
        payload.put("username", employeePO.getUsername());
        payload.put("shopId", employeePO.getShopId());

        String token = JWTUtil.createToken(payload, tokenKey.getBytes(StandardCharsets.UTF_8));
        redisTemplate.opsForValue().set(getTokenKey(employeePO.getShopId(), employeePO.getId()), token);
        return token;
    }

    private String getTokenKey(Integer shopId, Integer userId) {
        return String.format("token:%1$s:%2$s", shopId, userId);
    }

    @Override
    public boolean checkToken(String token) {
        boolean b = JWTUtil.verify(token, tokenKey.getBytes(StandardCharsets.UTF_8));
        if (!b) {
            return false;
        }

        JWT payload = JWTUtil.parseToken(token);
        if (Objects.isNull(payload)) {
            return false;
        }

        Integer userId = Integer.parseInt(payload.getPayload("id").toString());
        Integer shopId = Integer.parseInt(payload.getPayload("shopId").toString());

        long count = redisTemplate.countExistingKeys(Lists.newArrayList(getTokenKey(shopId, userId)));
        return count > 0;
    }
}
