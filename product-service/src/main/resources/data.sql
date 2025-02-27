-- Tạo sản phẩm Guitar ABC
INSERT INTO products (name, description, price, stock_quantity, image_url, category_id)
VALUES (
           'Guitar ABC',
           'Đàn guitar ABC chất lượng cao',
           9000000,
           5,
           'http://127.0.0.1:9001/musicstore/guita1.jfif',
           1
       );

-- Tạo sản phẩm Piano
INSERT INTO products (name, description, price, stock_quantity, image_url, category_id)
VALUES (
           'Piano XYZ',
           'Piano cao cấp, âm thanh hay',
           15000000,
           20,
           'http://127.0.0.1:9001/musicstore/piano1.jpg',
           2
       );
