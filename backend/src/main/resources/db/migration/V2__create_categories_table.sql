CREATE TABLE categories (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            name VARCHAR(64) NOT NULL,
                            parent_id BIGINT,
                            level INT NOT NULL DEFAULT 1,
                            sort_order INT NOT NULL DEFAULT 0,
                            icon_url VARCHAR(512),
                            status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
                            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                            INDEX idx_category_parent (parent_id, sort_order),
                            FOREIGN KEY (parent_id) REFERENCES categories(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;