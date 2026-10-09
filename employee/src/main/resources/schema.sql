CREATE TABLE IF NOT EXISTS tb_employee (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  shop_id INTEGER NOT NULL,
  username VARCHAR(64) NOT NULL,
  password VARCHAR(100) NOT NULL,
  avatar_url VARCHAR(500),
  last_login_time DATETIME,
  login_count INTEGER NOT NULL DEFAULT 0,
  status INTEGER NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS ux_employee_shop_username ON tb_employee(shop_id,username);
UPDATE tb_employee SET status=1,login_count=0 WHERE status IS NULL OR login_count IS NULL;
