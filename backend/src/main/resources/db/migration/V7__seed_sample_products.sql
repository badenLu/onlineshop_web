
-- Sample products for demonstration

-- Electronics > Smartphones (category_id = 4)
INSERT INTO products (category_id, name, description, price, original_price, stock, sales_count, main_image, images, status) VALUES
                                                                                                                                 (4, 'iPhone 15 Pro', '6.1-inch Super Retina XDR display, A17 Pro chip, titanium design. The most powerful iPhone ever.', 1199.00, 1299.00, 50, 328, 'https://picsum.photos/seed/iphone15/400/400', '["https://picsum.photos/seed/iphone15a/400/400","https://picsum.photos/seed/iphone15b/400/400"]', 'ACTIVE'),
                                                                                                                                 (4, 'Samsung Galaxy S24 Ultra', '6.8-inch Dynamic AMOLED, Snapdragon 8 Gen 3, built-in S Pen, AI-powered camera.', 1299.00, 1399.00, 35, 215, 'https://picsum.photos/seed/galaxy24/400/400', '["https://picsum.photos/seed/galaxy24a/400/400"]', 'ACTIVE'),
                                                                                                                                 (4, 'Google Pixel 8 Pro', '6.7-inch LTPO OLED, Tensor G3, best-in-class computational photography.', 899.00, 999.00, 40, 156, 'https://picsum.photos/seed/pixel8/400/400', '["https://picsum.photos/seed/pixel8a/400/400"]', 'ACTIVE'),
                                                                                                                                 (4, 'OnePlus 12', '6.82-inch AMOLED 120Hz, Snapdragon 8 Gen 3, 100W fast charging.', 799.00, NULL, 60, 89, 'https://picsum.photos/seed/oneplus12/400/400', '["https://picsum.photos/seed/oneplus12a/400/400"]', 'ACTIVE');

-- Electronics > Laptops (category_id = 5)
INSERT INTO products (category_id, name, description, price, original_price, stock, sales_count, main_image, images, status) VALUES
                                                                                                                                 (5, 'MacBook Pro 14" M3 Pro', 'Apple M3 Pro chip, 18GB RAM, 512GB SSD, Liquid Retina XDR display. Built for pros.', 1999.00, NULL, 25, 187, 'https://picsum.photos/seed/macbook14/400/400', '["https://picsum.photos/seed/macbook14a/400/400"]', 'ACTIVE'),
                                                                                                                                 (5, 'ThinkPad X1 Carbon Gen 11', '14-inch 2.8K OLED, Intel i7-1365U, 32GB RAM. The ultimate business ultrabook.', 1649.00, 1899.00, 20, 94, 'https://picsum.photos/seed/thinkpad/400/400', '["https://picsum.photos/seed/thinkpada/400/400"]', 'ACTIVE'),
                                                                                                                                 (5, 'Dell XPS 15', '15.6-inch 3.5K OLED, Intel i9-13900H, 32GB RAM, NVIDIA RTX 4060.', 1849.00, 2099.00, 15, 72, 'https://picsum.photos/seed/xps15/400/400', '["https://picsum.photos/seed/xps15a/400/400"]', 'ACTIVE'),
                                                                                                                                 (5, 'ASUS ROG Zephyrus G14', '14-inch QHD 165Hz, AMD Ryzen 9, RTX 4070, perfect portable gaming laptop.', 1599.00, NULL, 30, 143, 'https://picsum.photos/seed/rog14/400/400', '["https://picsum.photos/seed/rog14a/400/400"]', 'ACTIVE');

-- Clothing > Men (category_id = 6)
INSERT INTO products (category_id, name, description, price, original_price, stock, sales_count, main_image, images, status) VALUES
                                                                                                                                 (6, 'Classic Oxford Shirt', '100% premium cotton, slim fit, button-down collar. Perfect for business casual.', 59.99, 79.99, 200, 521, 'https://picsum.photos/seed/oxford/400/400', '["https://picsum.photos/seed/oxforda/400/400"]', 'ACTIVE'),
                                                                                                                                 (6, 'Wool Blend Overcoat', 'Italian wool blend, double-breasted design, satin lining. Timeless winter essential.', 249.00, 349.00, 40, 88, 'https://picsum.photos/seed/overcoat/400/400', '["https://picsum.photos/seed/overcoata/400/400"]', 'ACTIVE'),
                                                                                                                                 (6, 'Slim Fit Chinos', 'Stretch cotton twill, tapered leg, wrinkle-resistant. Available in 5 colors.', 49.99, NULL, 300, 634, 'https://picsum.photos/seed/chinos/400/400', '["https://picsum.photos/seed/chinosa/400/400"]', 'ACTIVE');

-- Clothing > Women (category_id = 7)
INSERT INTO products (category_id, name, description, price, original_price, stock, sales_count, main_image, images, status) VALUES
                                                                                                                                 (7, 'Cashmere V-Neck Sweater', 'Pure Mongolian cashmere, relaxed fit, ribbed cuffs. Luxury you can feel.', 189.00, 229.00, 80, 267, 'https://picsum.photos/seed/cashmere/400/400', '["https://picsum.photos/seed/cashmerea/400/400"]', 'ACTIVE'),
                                                                                                                                 (7, 'High-Waist Wide Leg Trousers', 'Flowing silhouette, elasticated waist, side pockets. Effortlessly elegant.', 79.99, NULL, 150, 389, 'https://picsum.photos/seed/trousers/400/400', '["https://picsum.photos/seed/trousersa/400/400"]', 'ACTIVE'),
                                                                                                                                 (7, 'Midi Wrap Dress', 'Floral print, V-neckline, adjustable waist tie. From office to dinner.', 89.99, 119.99, 100, 445, 'https://picsum.photos/seed/dress/400/400', '["https://picsum.photos/seed/dressa/400/400"]', 'ACTIVE');

-- Books (category_id = 3)
INSERT INTO products (category_id, name, description, price, original_price, stock, sales_count, main_image, images, status) VALUES
                                                                                                                                 (3, 'Clean Code by Robert C. Martin', 'A handbook of agile software craftsmanship. Essential reading for every developer.', 34.99, NULL, 500, 1203, 'https://picsum.photos/seed/cleancode/400/400', '["https://picsum.photos/seed/cleancodea/400/400"]', 'ACTIVE'),
                                                                                                                                 (3, 'Designing Data-Intensive Applications', 'By Martin Kleppmann. The big ideas behind reliable, scalable, and maintainable systems.', 39.99, 49.99, 300, 876, 'https://picsum.photos/seed/ddia/400/400', '["https://picsum.photos/seed/ddiaa/400/400"]', 'ACTIVE'),
                                                                                                                                 (3, 'Domain-Driven Design by Eric Evans', 'Tackling complexity in the heart of software. The original DDD reference.', 44.99, NULL, 200, 654, 'https://picsum.photos/seed/ddd/400/400', '["https://picsum.photos/seed/ddda/400/400"]', 'ACTIVE');

-- SKUs for each product
-- iPhone 15 Pro SKUs
INSERT INTO product_skus (product_id, sku_code, attributes, price, stock, status) VALUES
                                                                                      (1, 'IP15P-BLK-128', '{"color":"Black Titanium","storage":"128GB"}', 1199.00, 20, 'ACTIVE'),
                                                                                      (1, 'IP15P-WHT-128', '{"color":"White Titanium","storage":"128GB"}', 1199.00, 15, 'ACTIVE'),
                                                                                      (1, 'IP15P-BLK-256', '{"color":"Black Titanium","storage":"256GB"}', 1299.00, 15, 'ACTIVE');

-- Samsung Galaxy S24 Ultra SKUs
INSERT INTO product_skus (product_id, sku_code, attributes, price, stock, status) VALUES
                                                                                      (2, 'GS24U-BLK-256', '{"color":"Titanium Black","storage":"256GB"}', 1299.00, 20, 'ACTIVE'),
                                                                                      (2, 'GS24U-GRY-256', '{"color":"Titanium Gray","storage":"256GB"}', 1299.00, 15, 'ACTIVE');

-- Google Pixel 8 Pro SKUs
INSERT INTO product_skus (product_id, sku_code, attributes, price, stock, status) VALUES
                                                                                      (3, 'PX8P-OBS-128', '{"color":"Obsidian","storage":"128GB"}', 899.00, 20, 'ACTIVE'),
                                                                                      (3, 'PX8P-BAY-128', '{"color":"Bay","storage":"128GB"}', 899.00, 20, 'ACTIVE');

-- OnePlus 12 SKUs
INSERT INTO product_skus (product_id, sku_code, attributes, price, stock, status) VALUES
                                                                                      (4, 'OP12-BLK-256', '{"color":"Flowy Emerald","storage":"256GB"}', 799.00, 30, 'ACTIVE'),
                                                                                      (4, 'OP12-SLV-256', '{"color":"Silky Black","storage":"256GB"}', 799.00, 30, 'ACTIVE');

-- MacBook Pro SKUs
INSERT INTO product_skus (product_id, sku_code, attributes, price, stock, status) VALUES
                                                                                      (5, 'MBP14-SLV-512', '{"color":"Silver","storage":"512GB SSD"}', 1999.00, 15, 'ACTIVE'),
                                                                                      (5, 'MBP14-BLK-512', '{"color":"Space Black","storage":"512GB SSD"}', 1999.00, 10, 'ACTIVE');

-- ThinkPad SKUs
INSERT INTO product_skus (product_id, sku_code, attributes, price, stock, status) VALUES
                                                                                      (6, 'TP-X1C-16', '{"ram":"16GB","storage":"512GB"}', 1649.00, 10, 'ACTIVE'),
                                                                                      (6, 'TP-X1C-32', '{"ram":"32GB","storage":"1TB"}', 1899.00, 10, 'ACTIVE');

-- Dell XPS 15 SKUs
INSERT INTO product_skus (product_id, sku_code, attributes, price, stock, status) VALUES
                                                                                      (7, 'XPS15-16-512', '{"ram":"16GB","storage":"512GB"}', 1849.00, 8, 'ACTIVE'),
                                                                                      (7, 'XPS15-32-1T', '{"ram":"32GB","storage":"1TB"}', 2099.00, 7, 'ACTIVE');

-- ASUS ROG SKUs
INSERT INTO product_skus (product_id, sku_code, attributes, price, stock, status) VALUES
                                                                                      (8, 'ROG14-WHT', '{"color":"Moonlight White"}', 1599.00, 15, 'ACTIVE'),
                                                                                      (8, 'ROG14-GRY', '{"color":"Eclipse Gray"}', 1599.00, 15, 'ACTIVE');

-- Clothing SKUs (with size variants)
INSERT INTO product_skus (product_id, sku_code, attributes, price, stock, status) VALUES
                                                                                      (9, 'OXF-WHT-M', '{"color":"White","size":"M"}', 59.99, 50, 'ACTIVE'),
                                                                                      (9, 'OXF-WHT-L', '{"color":"White","size":"L"}', 59.99, 50, 'ACTIVE'),
                                                                                      (9, 'OXF-BLU-M', '{"color":"Light Blue","size":"M"}', 59.99, 50, 'ACTIVE'),
                                                                                      (9, 'OXF-BLU-L', '{"color":"Light Blue","size":"L"}', 59.99, 50, 'ACTIVE');

INSERT INTO product_skus (product_id, sku_code, attributes, price, stock, status) VALUES
                                                                                      (10, 'OVC-BLK-M', '{"color":"Black","size":"M"}', 249.00, 20, 'ACTIVE'),
                                                                                      (10, 'OVC-BLK-L', '{"color":"Black","size":"L"}', 249.00, 20, 'ACTIVE');

INSERT INTO product_skus (product_id, sku_code, attributes, price, stock, status) VALUES
                                                                                      (11, 'CHN-BEG-32', '{"color":"Beige","size":"32"}', 49.99, 60, 'ACTIVE'),
                                                                                      (11, 'CHN-NVY-32', '{"color":"Navy","size":"32"}', 49.99, 60, 'ACTIVE'),
                                                                                      (11, 'CHN-BLK-32', '{"color":"Black","size":"32"}', 49.99, 60, 'ACTIVE');

INSERT INTO product_skus (product_id, sku_code, attributes, price, stock, status) VALUES
                                                                                      (12, 'CSH-CRM-S', '{"color":"Cream","size":"S"}', 189.00, 20, 'ACTIVE'),
                                                                                      (12, 'CSH-CRM-M', '{"color":"Cream","size":"M"}', 189.00, 30, 'ACTIVE'),
                                                                                      (12, 'CSH-GRY-M', '{"color":"Gray","size":"M"}', 189.00, 30, 'ACTIVE');

INSERT INTO product_skus (product_id, sku_code, attributes, price, stock, status) VALUES
                                                                                      (13, 'WLT-BLK-S', '{"color":"Black","size":"S"}', 79.99, 40, 'ACTIVE'),
                                                                                      (13, 'WLT-BLK-M', '{"color":"Black","size":"M"}', 79.99, 40, 'ACTIVE');

INSERT INTO product_skus (product_id, sku_code, attributes, price, stock, status) VALUES
                                                                                      (14, 'DRS-FLR-S', '{"color":"Floral","size":"S"}', 89.99, 30, 'ACTIVE'),
                                                                                      (14, 'DRS-FLR-M', '{"color":"Floral","size":"M"}', 89.99, 35, 'ACTIVE');

-- Book SKUs (single variant)
INSERT INTO product_skus (product_id, sku_code, attributes, price, stock, status) VALUES
                                                                                      (15, 'CC-PAPER', '{"format":"Paperback"}', 34.99, 500, 'ACTIVE'),
                                                                                      (16, 'DDIA-PAPER', '{"format":"Paperback"}', 39.99, 300, 'ACTIVE'),
                                                                                      (17, 'DDD-PAPER', '{"format":"Paperback"}', 44.99, 200, 'ACTIVE');

-- Test user address for demo
INSERT INTO user_addresses (user_id, receiver_name, phone, province, city, district, detail_address, is_default) VALUES
    (1, 'Admin User', '+49 170 1234567', 'Baden-Württemberg', 'Stuttgart', 'Mitte', 'Königstraße 1, 70173', true);