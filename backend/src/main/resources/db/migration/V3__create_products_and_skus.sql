CREATE TABLE products (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          category_id BIGINT NOT NULL,
                          name VARCHAR(200) NOT NULL,
                          description TEXT,
                          price DECIMAL(10,2) NOT NULL,
                          original_price DECIMAL(10,2),
                          stock INT NOT NULL DEFAULT 0,
                          sales_count INT NOT NULL DEFAULT 0,
                          main_image VARCHAR(512) NOT NULL,
                          images JSON,
                          status VARCHAR(16) NOT NULL DEFAULT 'DRAFT',
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                          INDEX idx_product_category_status (category_id, status, created_at DESC),
                          INDEX idx_product_sales (sales_count DESC),
                          FOREIGN KEY (category_id) REFERENCES categories(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE product_skus (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              product_id BIGINT NOT NULL,
                              sku_code VARCHAR(64) NOT NULL,
                              attributes JSON NOT NULL,
                              price DECIMAL(10,2) NOT NULL,
                              stock INT NOT NULL DEFAULT 0,
                              status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
                              created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                              CONSTRAINT uk_sku_code UNIQUE (sku_code),
                              INDEX idx_sku_product (product_id, status),
                              FOREIGN KEY (product_id) REFERENCES products(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;