-- Tạo database `auth_db` cho Auth Service
CREATE DATABASE IF NOT EXISTS auth_db;

-- Tạo database `musicstore` cho Product Service
CREATE DATABASE IF NOT EXISTS musicstore;

-- Cấp quyền cho user root
GRANT ALL PRIVILEGES ON auth_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON musicstore.* TO 'root'@'%';
FLUSH PRIVILEGES;
