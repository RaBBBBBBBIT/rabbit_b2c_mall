package com.rabbit.mall.common.auth;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;

import java.nio.charset.StandardCharsets;

/** Identity decoded from the employee service's signed bearer token. */
public final class AuthenticatedUser {
    private final Integer employeeId;
    private final Integer shopId;
    private final String username;

    private AuthenticatedUser(Integer employeeId, Integer shopId, String username) {
        this.employeeId = employeeId;
        this.shopId = shopId;
        this.username = username;
    }

    public static AuthenticatedUser from(String authorization, String signingKey) {
        if (authorization == null || authorization.trim().isEmpty()) {
            throw new IllegalArgumentException("Authorization bearer token is required");
        }
        String token = authorization.startsWith("Bearer ") ? authorization.substring(7).trim() : authorization.trim();
        byte[] key = signingKey.getBytes(StandardCharsets.UTF_8);
        if (!JWTUtil.verify(token, key)) throw new IllegalArgumentException("Invalid token");
        JWT jwt = JWTUtil.parseToken(token);
        Object employeeId = jwt == null ? null : jwt.getPayload("id");
        Object shopId = jwt == null ? null : jwt.getPayload("shopId");
        Object username = jwt == null ? null : jwt.getPayload("username");
        Object expiresAt = jwt == null ? null : jwt.getPayload("exp");
        if (employeeId == null || shopId == null || expiresAt == null
                || Long.parseLong(expiresAt.toString()) <= System.currentTimeMillis() / 1000L) {
            throw new IllegalArgumentException("Expired or incomplete token");
        }
        return new AuthenticatedUser(Integer.valueOf(employeeId.toString()), Integer.valueOf(shopId.toString()),
                username == null ? "" : username.toString());
    }

    public Integer getEmployeeId() { return employeeId; }
    public Integer getShopId() { return shopId; }
    public String getUsername() { return username; }
}
