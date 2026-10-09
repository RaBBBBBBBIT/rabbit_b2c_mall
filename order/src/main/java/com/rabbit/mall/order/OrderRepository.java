package com.rabbit.mall.order;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class OrderRepository {
    private final JdbcTemplate jdbc;
    private final RowMapper<OrderRecord> orderMapper = new RowMapper<OrderRecord>() {
        @Override public OrderRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
            OrderRecord o = new OrderRecord(); o.id=rs.getInt("id"); o.orderNo=rs.getString("order_no");
            o.shopId=rs.getInt("shop_id"); o.employeeId=rs.getInt("employee_id"); o.total=rs.getBigDecimal("total_amount");
            o.status=rs.getString("status"); o.createdAt=rs.getString("created_at"); o.updatedAt=rs.getString("updated_at");
            return o;
        }
    };
    public OrderRepository(JdbcTemplate jdbc) { this.jdbc=jdbc; }
    public OrderRecord create(Integer shopId, Integer employeeId, String orderNo, BigDecimal total) {
        jdbc.update("INSERT INTO orders(order_no,shop_id,employee_id,total_amount,status) VALUES(?,?,?,?, 'WAIT_PAY')",
                orderNo, shopId, employeeId, total);
        return findById(jdbc.queryForObject("SELECT last_insert_rowid()", Integer.class)).get();
    }
    public void addItem(Integer orderId, OrderItem item) {
        jdbc.update("INSERT INTO order_items(order_id,product_id,product_name,unit_price,quantity,subtotal) VALUES(?,?,?,?,?,?)",
                orderId,item.productId,item.productName,item.unitPrice,item.quantity,item.subtotal);
    }
    public void addLog(Integer orderId, String from, String to, Integer actorId, String note) {
        jdbc.update("INSERT INTO order_status_logs(order_id,from_status,to_status,actor_id,note) VALUES(?,?,?,?,?)",
                orderId,from,to,actorId,note);
    }
    public Optional<OrderRecord> findById(Integer id) {
        List<OrderRecord> rows=jdbc.query("SELECT * FROM orders WHERE id=?",orderMapper,id);
        return rows.isEmpty()?Optional.<OrderRecord>empty():Optional.of(rows.get(0));
    }
    public Optional<OrderRecord> findByIdAndShop(Integer id,Integer shopId) {
        List<OrderRecord> rows=jdbc.query("SELECT * FROM orders WHERE id=? AND shop_id=?",orderMapper,id,shopId);
        return rows.isEmpty()?Optional.<OrderRecord>empty():Optional.of(rows.get(0));
    }
    public List<OrderRecord> list(Integer shopId) { return jdbc.query("SELECT * FROM orders WHERE shop_id=? ORDER BY id DESC",orderMapper,shopId); }
    public List<OrderItem> items(Integer id) {
        return jdbc.query("SELECT * FROM order_items WHERE order_id=? ORDER BY id",(rs,n)-> {
            OrderItem i=new OrderItem(); i.productId=rs.getInt("product_id"); i.productName=rs.getString("product_name");
            i.unitPrice=rs.getBigDecimal("unit_price"); i.quantity=rs.getInt("quantity"); i.subtotal=rs.getBigDecimal("subtotal"); return i;
        },id);
    }
    public int transition(Integer id, String from, String to) {
        return jdbc.update("UPDATE orders SET status=?,updated_at=CURRENT_TIMESTAMP WHERE id=? AND status=?",to,id,from);
    }
    public static class OrderRecord {
        public Integer id,shopId,employeeId; public String orderNo,status,createdAt,updatedAt; public BigDecimal total;
    }
    public static class OrderItem {
        public Integer productId,quantity; public String productName; public BigDecimal unitPrice,subtotal;
    }
}
