-- Admin user (password: admin123, BCrypt encoded)
INSERT INTO users (username, email, password_hash, role) VALUES
    ('admin', 'admin@shop.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN');

-- Sample categories
INSERT INTO categories (name, parent_id, level, sort_order) VALUES
                                                                ('Electronics', NULL, 1, 1),
                                                                ('Clothing', NULL, 1, 2),
                                                                ('Books', NULL, 1, 3);

INSERT INTO categories (name, parent_id, level, sort_order) VALUES
                                                                ('Smartphones', 1, 2, 1),
                                                                ('Laptops', 1, 2, 2),
                                                                ('Men', 2, 2, 1),
                                                                ('Women', 2, 2, 2);