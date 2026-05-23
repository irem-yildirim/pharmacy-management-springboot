-- src/main/resources/data.sql

-- BRANDS
INSERT INTO brand (id, name, is_active) VALUES (1, 'Pfizer', 1);
INSERT INTO brand (id, name, is_active) VALUES (2, 'Abdi İbrahim', 1);
INSERT INTO brand (id, name, is_active) VALUES (3, 'Sanofi', 1);
INSERT INTO brand (id, name, is_active) VALUES (4, 'Novartis', 1);
INSERT INTO brand (id, name, is_active) VALUES (5, 'Bilim İlaç', 1);

-- CATEGORIES
INSERT INTO category (id, name, is_active) VALUES (1, 'Analjezik & Antienflamatuar (Pain & Inflammation)', 1);
INSERT INTO category (id, name, is_active) VALUES (2, 'Sistemik Antibakteriyeller (Antibiotics)', 1);
INSERT INTO category (id, name, is_active) VALUES (3, 'Psikoleptikler (Psychiatry)', 1);
INSERT INTO category (id, name, is_active) VALUES (4, 'Vitaminler ve Mineraller (Supplements)', 1);
INSERT INTO category (id, name, is_active) VALUES (5, 'Kardiyovasküler Sistem (Cardiology)', 1);

-- PRESTYPE
INSERT INTO pres_type (id, name, risk_level) VALUES (1, 'Normal Reçete', 0);
INSERT INTO pres_type (id, name, risk_level) VALUES (2, 'Beyaz Reçete', 1);
INSERT INTO pres_type (id, name, risk_level) VALUES (3, 'Yeşil Reçete', 2);
INSERT INTO pres_type (id, name, risk_level) VALUES (4, 'Kırmızı Reçete', 3);

-- DRUGS
INSERT INTO drug (barcode, name, category_id, brand_id, pres_id, current_selling_price, min_stock_alert, is_active, version)
VALUES ('8699514010686', 'Parol 500 mg Tablet', 1, 2, 1, 45.50, 100, 1, 0);

INSERT INTO drug (barcode, name, category_id, brand_id, pres_id, current_selling_price, min_stock_alert, is_active, version)
VALUES ('8699504090564', 'Augmentin 1000 mg BID', 2, 5, 2, 125.00, 30, 1, 0);

INSERT INTO drug (barcode, name, category_id, brand_id, pres_id, current_selling_price, min_stock_alert, is_active, version)
VALUES ('8699532040034', 'Xanax 0.5 mg Tablet', 3, 1, 3, 85.50, 15, 1, 0);

INSERT INTO drug (barcode, name, category_id, brand_id, pres_id, current_selling_price, min_stock_alert, is_active, version)
VALUES ('8699593090044', 'Concerta 36 mg Tablet', 3, 1, 4, 420.00, 10, 1, 0);

INSERT INTO drug (barcode, name, category_id, brand_id, pres_id, current_selling_price, min_stock_alert, is_active, version)
VALUES ('8699809090038', 'Pharmaton Vitality 30 Kapsül', 4, 3, 1, 350.00, 20, 1, 0);

INSERT INTO drug (barcode, name, category_id, brand_id, pres_id, current_selling_price, min_stock_alert, is_active, version)
VALUES ('8699508090409', 'Beloc Zok 50 mg', 5, 4, 1, 95.25, 40, 1, 0);

-- USERS (Password 'password123' BCrypt hash: $2a$10$8.27U0KzQ0mI/XWjR/X55O9sB7Yv.VfRzOQ2P2r8pG46mD42uH9wK)
INSERT INTO users (id, name, username, password, role, is_active)
VALUES (1, 'Admin User', 'admin', '$2a$10$8.27U0KzQ0mI/XWjR/X55O9sB7Yv.VfRzOQ2P2r8pG46mD42uH9wK', 'ADMIN', 1);

INSERT INTO users (id, name, username, password, role, is_active)
VALUES (2, 'Pharmacist User', 'eczaci_ayse', '$2a$10$8.27U0KzQ0mI/XWjR/X55O9sB7Yv.VfRzOQ2P2r8pG46mD42uH9wK', 'PHARMACIST', 1);

-- CUSTOMERS
INSERT INTO customer (id, name, phone, balance, is_active)
VALUES (1, 'Ahmet Yılmaz', '5551234567', 250.50, 1);

INSERT INTO customer (id, name, phone, balance, is_active)
VALUES (2, 'Ayşe Demir', '5557654321', 0.00, 1);

INSERT INTO customer (id, name, phone, balance, is_active)
VALUES (3, 'Mehmet Kaya', '5559876543', 1450.00, 1);

-- PURCHASES (FIFO batches linked to drugs)
-- SCENARIO 1 (Healthy Stock): Parol - 1 batch of 500 units, expiring in 400 days (2027-06-27). Purchase price: 30.00.
INSERT INTO purchase (id, drug_barcode, original_quantity, remaining_quantity, purchase_price, expiration_date, purchase_date)
VALUES (1, '8699514010686', 500, 500, 30.00, '2027-06-27', '2026-05-23');

-- SCENARIO 2 (FIFO Split): Augmentin
-- Batch A: 20 remaining of 100, expiring in 15 days (2026-06-07). Purchase price: 90.00.
INSERT INTO purchase (id, drug_barcode, original_quantity, remaining_quantity, purchase_price, expiration_date, purchase_date)
VALUES (2, '8699504090564', 100, 20, 90.00, '2026-06-07', '2025-06-07');

-- Batch B: 100 remaining of 100, expiring in 180 days (2026-11-19). Purchase price: 95.00.
INSERT INTO purchase (id, drug_barcode, original_quantity, remaining_quantity, purchase_price, expiration_date, purchase_date)
VALUES (3, '8699504090564', 100, 100, 95.00, '2026-11-19', '2025-11-19');

-- SCENARIO 3 (Completely Expired): Xanax - 5 remaining of 50, expired 10 days ago (2026-05-13). Purchase price: 60.00.
INSERT INTO purchase (id, drug_barcode, original_quantity, remaining_quantity, purchase_price, expiration_date, purchase_date)
VALUES (4, '8699532040034', 50, 5, 60.00, '2026-05-13', '2024-05-13');

-- SCENARIO 4 (Low Stock Warning): Concerta - 4 remaining of 20, expiring in 300 days (2027-03-19). Purchase price: 300.00.
INSERT INTO purchase (id, drug_barcode, original_quantity, remaining_quantity, purchase_price, expiration_date, purchase_date)
VALUES (5, '8699593090044', 20, 4, 300.00, '2027-03-19', '2026-03-19');
