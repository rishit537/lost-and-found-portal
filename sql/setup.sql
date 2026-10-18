
-- DB setup script

CREATE DATABASE IF NOT EXISTS lost_and_found_portal
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE lost_and_found_portal;

-- storing the users
CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone_number VARCHAR(20)
);

-- categories table
CREATE TABLE IF NOT EXISTS categories (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(100) NOT NULL UNIQUE
);

-- Pre-defined categories
INSERT IGNORE INTO categories (category_name) VALUES
    ('Electronics'),
    ('Accessories'),
    ('Documents'),
    ('Stationery'),
    ('Clothing'),
    ('Sports'),
    ('Other');

-- items table
CREATE TABLE IF NOT EXISTS items (
    item_id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    location VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'Lost'
        CHECK (status IN ('Lost', 'Found', 'Claimed')),
    image_data LONGBLOB,
    category_id INT NOT NULL,
    reported_by_user_id INT NOT NULL,
    date_reported TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_items_category
        FOREIGN KEY (category_id) REFERENCES categories(category_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,

    CONSTRAINT fk_items_user
        FOREIGN KEY (reported_by_user_id) REFERENCES users(user_id)
        ON UPDATE CASCADE ON DELETE CASCADE
);

-- claim log
CREATE TABLE IF NOT EXISTS claims (
    claim_id INT AUTO_INCREMENT PRIMARY KEY,
    item_id INT NOT NULL,
    claimed_by_user_id INT NOT NULL,
    claim_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_claims_item
        FOREIGN KEY (item_id) REFERENCES items(item_id)
        ON UPDATE CASCADE ON DELETE CASCADE,

    CONSTRAINT fk_claims_user
        FOREIGN KEY (claimed_by_user_id) REFERENCES users(user_id)
        ON UPDATE CASCADE ON DELETE CASCADE
);
