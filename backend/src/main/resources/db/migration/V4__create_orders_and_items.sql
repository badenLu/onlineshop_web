CREATE TABLE orders (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        user_id BIGINT NOT NULL,
                        order_no VARCHAR(32) NOT NULL,
                        total_amount DECIMAL(10,2) NOT NULL,
                        pay_amount DECIMAL(10,2) NOT NULL,
                        freight DECIMAL(10,2) NOT NULL DEFAULT 0.00,
                        status VARCHAR(20) NOT NULL DEFAULT 'PENDING_PAYMENT',
                        payment_type VARCHAR(20),
                        payment_time TIMESTAMP NULL,
                        delivery_time TIMESTAMP NULL,
                        receive_time TIMESTAMP NULL,
                        receiver_name VARCHAR(32) NOT NULL,
                        receiver_phone VARCHAR(20) NOT NULL,
                        receiver_address VARCHAR(500) NOT NULL,
                        remark VARCHAR(500),
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                        CONSTRAINT uk_order_no UNIQUE (order_no),
                        INDEX idx_order_user_status (user_id, status, created_at DESC),
                        FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE order_items (
                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             order_id BIGINT NOT NULL,
                             product_id BIGINT NOT NULL,
                             sku_id BIGINT NOT NULL,
                             product_name VARCHAR(200) NOT NULL,
                             product_image VARCHAR(512) NOT NULL,
                             sku_attributes VARCHAR(500),
                             unit_price DECIMAL(10,2) NOT NULL,
                             quantity INT NOT NULL,
                             total_price DECIMAL(10,2) NOT NULL,
                             created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                             INDEX idx_item_order (order_id),
                             FOREIGN KEY (order_id) REFERENCES orders(id),
                             FOREIGN KEY (product_id) REFERENCES products(id),
                             FOREIGN KEY (sku_id) REFERENCES product_skus(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;