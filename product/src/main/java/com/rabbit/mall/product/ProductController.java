package com.rabbit.mall.product;

import com.rabbit.mall.common.auth.AuthenticatedUser;
import com.rabbit.mall.common.response.BaseResponseVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/product")
public class ProductController {
    private final ProductRepository repository;
    private final RestTemplate http;
    @Value("${jwt.token.key}") private String jwtKey;
    @Value("${mall.internal.token}") private String internalToken;
    @Value("${employee.service-url}") private String employeeServiceUrl;

    public ProductController(ProductRepository repository, RestTemplate http) { this.repository = repository; this.http = http; }

    @PostMapping("/create")
    public BaseResponseVO<Product> create(@RequestHeader("Authorization") String authorization,
                                          @Valid @RequestBody CreateProductRequest request) {
        AuthenticatedUser user = user(authorization);
        if (!"physical".equals(request.type) && !"virtual".equals(request.type))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"type must be physical or virtual");
        Product p = new Product(); p.setShopId(user.getShopId()); p.setCreatorId(user.getEmployeeId());
        p.setName(request.name.trim()); p.setDescription(request.description); p.setType(request.type);
        p.setPrice(request.price); p.setStock(request.stock);
        return new BaseResponseVO<>(repository.save(p));
    }

    @GetMapping("/list")
    public BaseResponseVO<List<Product>> list(@RequestHeader("Authorization") String authorization,
                                               @RequestParam(required = false) Integer status) {
        AuthenticatedUser user = user(authorization);
        if (status != null && status != 0 && status != 1) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "status must be 0 or 1");
        return new BaseResponseVO<>(repository.list(user.getShopId(), status));
    }

    @GetMapping("/{id}")
    public BaseResponseVO<Product> get(@RequestHeader("Authorization") String authorization, @PathVariable Integer id) {
        AuthenticatedUser user = user(authorization);
        Product p = repository.findById(id).filter(item -> item.getShopId().equals(user.getShopId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "product not found"));
        return new BaseResponseVO<>(p);
    }

    @PostMapping("/{id}/status")
    public BaseResponseVO<Boolean> status(@RequestHeader("Authorization") String authorization,
                                          @PathVariable Integer id, @RequestParam Integer status) {
        AuthenticatedUser user = user(authorization);
        if (status == null || (status != 0 && status != 1)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "status must be 0 or 1");
        if (repository.updateStatus(id, user.getShopId(), status) != 1) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "product not found");
        return new BaseResponseVO<>(true);
    }

    @PostMapping("/internal/{id}/reserve")
    public BaseResponseVO<Boolean> reserve(@RequestHeader(value="X-Internal-Token", defaultValue="") String token,
                                           @PathVariable Integer id, @RequestParam @Min(1) int quantity) {
        checkInternal(token);
        if (quantity < 1) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"quantity must be positive");
        if (repository.reserve(id, quantity) != 1) throw new ResponseStatusException(HttpStatus.CONFLICT, "product unavailable or insufficient stock");
        return new BaseResponseVO<>(true);
    }

    @PostMapping("/internal/{id}/release")
    public BaseResponseVO<Boolean> release(@RequestHeader(value="X-Internal-Token", defaultValue="") String token,
                                           @PathVariable Integer id, @RequestParam @Min(1) int quantity) {
        checkInternal(token);
        if (quantity < 1) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"quantity must be positive");
        if (repository.release(id, quantity) != 1) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "product not found");
        return new BaseResponseVO<>(true);
    }

    private AuthenticatedUser user(String authorization) {
        AuthenticatedUser user;
        try { user = AuthenticatedUser.from(authorization, jwtKey); }
        catch (RuntimeException ex) { throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid or expired token"); }
        HttpHeaders headers = new HttpHeaders(); headers.set(HttpHeaders.AUTHORIZATION, authorization); headers.set("X-Internal-Token", internalToken);
        try {
            ResponseEntity<Map> response = http.exchange(employeeServiceUrl + "/employee/internal/validate", HttpMethod.POST, new HttpEntity<>(headers), Map.class);
            if (response.getBody() == null || !Boolean.TRUE.equals(response.getBody().get("data")))
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid or expired token");
            return user;
        } catch (ResponseStatusException ex) { throw ex; }
        catch (RuntimeException ex) { throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "authentication service unavailable", ex); }
    }
    private void checkInternal(String supplied) {
        if (!java.security.MessageDigest.isEqual(internalToken.getBytes(java.nio.charset.StandardCharsets.UTF_8), supplied.getBytes(java.nio.charset.StandardCharsets.UTF_8)))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden");
    }

    public static class CreateProductRequest {
        @NotBlank @Size(max=120) public String name;
        @Size(max=2000) public String description;
        @NotBlank public String type = "physical";
        @NotNull @DecimalMin(value="0.01") public BigDecimal price;
        @NotNull @Min(0) public Integer stock;
    }
}
