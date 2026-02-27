CREATE TABLE shopping_cart (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               user_id BIGINT NOT NULL,
                               product_id BIGINT NOT NULL,
                               sku_id BIGINT NOT NULL,
                               quantity INT NOT NULL DEFAULT 1,
                               selected BOOLEAN NOT NULL DEFAULT TRUE,
                               created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                               CONSTRAINT uk_cart_user_sku UNIQUE (user_id, sku_id),
                               FOREIGN KEY (user_id) REFERENCES users(id),
                               FOREIGN KEY (sku_id) REFERENCES product_skus(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE user_addresses (
                                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                user_id BIGINT NOT NULL,
                                receiver_name VARCHAR(32) NOT NULL,
                                phone VARCHAR(20) NOT NULL,
                                province VARCHAR(32) NOT NULL,
                                city VARCHAR(32) NOT NULL,
                                district VARCHAR(32) NOT NULL,
                                detail_address VARCHAR(200) NOT NULL,
                                is_default BOOLEAN NOT NULL DEFAULT FALSE,
                                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                                INDEX idx_address_user (user_id),
                                FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE payment_records (
                                 id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 order_id BIGINT NOT NULL,
                                 payment_no VARCHAR(64) NOT NULL,
                                 payment_type VARCHAR(20) NOT NULL,
                                 amount DECIMAL(10,2) NOT NULL,
                                 status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
                                 callback_data JSON,
                                 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                 INDEX idx_payment_order (order_id),
                                 INDEX idx_payment_no (payment_no),
                                 FOREIGN KEY (order_id) REFERENCES orders(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;