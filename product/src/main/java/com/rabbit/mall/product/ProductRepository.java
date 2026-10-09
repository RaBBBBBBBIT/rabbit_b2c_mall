package com.rabbit.mall.product;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class ProductRepository {
    private final JdbcTemplate jdbc;
    private final RowMapper<Product> rowMapper = new RowMapper<Product>() {
        @Override public Product mapRow(ResultSet rs, int rowNum) throws SQLException {
            Product p = new Product();
            p.setId(rs.getInt("id")); p.setShopId(rs.getInt("shop_id")); p.setCreatorId(rs.getInt("creator_id"));
            p.setName(rs.getString("name")); p.setDescription(rs.getString("description")); p.setType(rs.getString("type"));
            p.setPrice(rs.getBigDecimal("price")); p.setStock(rs.getInt("stock")); p.setStatus(rs.getInt("status"));
            p.setCreatedAt(rs.getTimestamp("created_at").toInstant());
            return p;
        }
    };

    public ProductRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Transactional
    public Product save(Product p) {
        jdbc.update("INSERT INTO products(shop_id,creator_id,name,description,type,price,stock,status) VALUES(?,?,?,?,?,?,?,0)",
                p.getShopId(), p.getCreatorId(), p.getName(), p.getDescription(), p.getType(), p.getPrice(), p.getStock());
        return findById(jdbc.queryForObject("SELECT last_insert_rowid()", Integer.class)).get();
    }

    public Optional<Product> findById(Integer id) {
        List<Product> rows = jdbc.query("SELECT * FROM products WHERE id=?", rowMapper, id);
        return rows.isEmpty() ? Optional.<Product>empty() : Optional.of(rows.get(0));
    }

    public List<Product> list(Integer shopId, Integer status) {
        if (status == null) return jdbc.query("SELECT * FROM products WHERE shop_id=? ORDER BY id DESC", rowMapper, shopId);
        return jdbc.query("SELECT * FROM products WHERE shop_id=? AND status=? ORDER BY id DESC", rowMapper, shopId, status);
    }

    public int updateStatus(Integer id, Integer shopId, Integer status) {
        return jdbc.update("UPDATE products SET status=?, updated_at=CURRENT_TIMESTAMP WHERE id=? AND shop_id=?", status, id, shopId);
    }

    public int reserve(Integer id, int quantity) {
        return jdbc.update("UPDATE products SET stock=stock-?, updated_at=CURRENT_TIMESTAMP WHERE id=? AND status=1 AND stock>=?",
                quantity, id, quantity);
    }

    public int release(Integer id, int quantity) {
        return jdbc.update("UPDATE products SET stock=stock+?, updated_at=CURRENT_TIMESTAMP WHERE id=?", quantity, id);
    }
}
