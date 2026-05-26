-- Pharmacy Management System - Presentation Seed Data
-- Generated for June 1, 2026 presentation
-- Run this script on a fresh pharmacy_db database

-- Clean all tables (order matters for FK constraints)
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE sale_item;
TRUNCATE TABLE sale;
TRUNCATE TABLE purchase;
TRUNCATE TABLE drug;
TRUNCATE TABLE customer;
TRUNCATE TABLE users;
TRUNCATE TABLE pres_type;
TRUNCATE TABLE brand;
TRUNCATE TABLE category;
SET FOREIGN_KEY_CHECKS = 1;

-- PRESCRIPTION TYPES
INSERT INTO pres_type (id, name, risk_level) VALUES
(1, 'White', 1),
(2, 'Orange', 2),
(3, 'Purple', 2),
(4, 'Green', 3),
(5, 'Red', 4);

-- BRANDS (5 major pharmaceutical companies)
INSERT INTO brand (id, name, is_active) VALUES
(1, 'Pfizer', 1),
(2, 'Novartis', 1),
(3, 'Bayer', 1),
(4, 'Sanofi', 1),
(5, 'Abdi İbrahim', 1);

-- CATEGORIES (6 therapeutic classes)
INSERT INTO category (id, name, is_active) VALUES
(1, 'Antibiyotikler', 1),
(2, 'Ağrı Kesiciler', 1),
(3, 'Antiviral İlaçlar', 1),
(4, 'Kardiyovasküler', 1),
(5, 'Gastrointestinal', 1),
(6, 'Psikiyatrik İlaçlar', 1);

-- USERS (BCrypt hash for 'password123')
INSERT INTO users (id, name, username, password, role, is_active) VALUES
(1, 'Admin Yönetici', 'admin', '$2a$10$8.27U0KzQ0mI/XWjR/X55O9sB7Yv.VfRzOQ2P2r8pG46mD42uH9wK', 'ADMIN', 1),
(2, 'Ecz. Ayşe Yılmaz', 'eczaci_ayse', '$2a$10$8.27U0KzQ0mI/XWjR/X55O9sB7Yv.VfRzOQ2P2r8pG46mD42uH9wK', 'PHARMACIST', 1),
(3, 'Ecz. Mehmet Demir', 'eczaci_mehmet', '$2a$10$8.27U0KzQ0mI/XWjR/X55O9sB7Yv.VfRzOQ2P2r8pG46mD42uH9wK', 'PHARMACIST', 1),
(4, 'Kasiyer Veli Kaya', 'kasiyer_veli', '$2a$10$8.27U0KzQ0mI/XWjR/X55O9sB7Yv.VfRzOQ2P2r8pG46mD42uH9wK', 'CASHIER', 1),
(5, 'Kasiyer Zeynep Öztürk', 'kasiyer_zeynep', '$2a$10$8.27U0KzQ0mI/XWjR/X55O9sB7Yv.VfRzOQ2P2r8pG46mD42uH9wK', 'CASHIER', 1);

-- CUSTOMERS (5 registered patients)
INSERT INTO customer (id, name, phone, balance, is_active) VALUES
(1, 'Ahmet Yılmaz', '5551234567', 1250.50, 1),
(2, 'Ayşe Demir', '5557654321', 340.00, 1),
(3, 'Mehmet Kaya', '5559876543', 2780.75, 1),
(4, 'Fatma Şahin', '5553456789', 520.25, 1),
(5, 'Ali Özdemir', '5551122334', 890.00, 1);

-- DRUGS (15 pharmaceutical products)
INSERT INTO drug (barcode, name, category_id, brand_id, pres_id, current_selling_price, min_stock_alert, is_active, version) VALUES
('8699514010686', 'Parol 500 mg Tablet', 2, 5, 1, 45.50, 100, 1, 0),
('8699504090564', 'Augmentin 1000 mg BID', 1, 3, 1, 125.00, 30, 1, 0),
('8699532040034', 'Xanax 0.5 mg Tablet', 6, 1, 4, 85.50, 15, 1, 0),
('8699593090044', 'Concerta 36 mg Tablet', 6, 2, 5, 420.00, 10, 1, 0),
('8699809090038', 'Pharmaton Vitality 30 Kapsül', 2, 4, 1, 350.00, 20, 1, 0),
('8699508090409', 'Beloc Zok 50 mg Tablet', 4, 2, 1, 95.25, 40, 1, 0),
('8699522010045', 'Zinnat 500 mg Tablet', 1, 3, 1, 210.00, 20, 1, 0),
('8699544010072', 'Lipitor 20 mg Tablet', 4, 1, 1, 175.50, 50, 1, 0),
('8699566010098', 'Nexium 40 mg Tablet', 5, 4, 1, 290.00, 15, 1, 0),
('8699588010124', 'Ritalin 10 mg Tablet', 6, 2, 5, 380.00, 12, 1, 0),
('8699610010155', 'Tamiflu 75 mg Kapsül', 3, 1, 4, 520.00, 8, 1, 0),
('8699632010186', 'Voltaren 50 mg Tablet', 2, 2, 1, 65.00, 60, 1, 0),
('8699654010216', 'Cipro 500 mg Tablet', 1, 3, 1, 185.00, 25, 1, 0),
('8699676010247', 'Risperdal 2 mg Tablet', 6, 1, 3, 450.00, 10, 1, 0),
('8699698010278', 'Gaviscon Advance Likit', 5, 5, 1, 78.90, 35, 1, 0);

-- PURCHASE BATCHES (3 per drug = 45 batches, FIFO-ready)
-- Batch 1: EXPIRED (May 28, 2026 - before June 1 presentation)
-- Batch 2: CRITICAL (June 17, 2026 - 16 days from June 1)
-- Batch 3: SAFE (March 15, 2027)

INSERT INTO purchase (id, drug_barcode, original_quantity, remaining_quantity, purchase_price, expiration_date, purchase_date) VALUES
-- Parol
(1, '8699514010686', 30, 30, 25.03, '2026-05-28', '2026-03-27'),
(2, '8699514010686', 50, 50, 28.21, '2026-06-17', '2026-04-26'),
(3, '8699514010686', 200, 200, 26.39, '2027-03-15', '2026-05-21'),
-- Augmentin
(4, '8699504090564', 30, 30, 68.75, '2026-05-28', '2026-03-27'),
(5, '8699504090564', 50, 50, 77.50, '2026-06-17', '2026-04-26'),
(6, '8699504090564', 200, 200, 72.50, '2027-03-15', '2026-05-21'),
-- Xanax
(7, '8699532040034', 30, 30, 47.03, '2026-05-28', '2026-03-27'),
(8, '8699532040034', 50, 50, 53.01, '2026-06-17', '2026-04-26'),
(9, '8699532040034', 200, 200, 49.59, '2027-03-15', '2026-05-21'),
-- Concerta
(10, '8699593090044', 30, 30, 231.00, '2026-05-28', '2026-03-27'),
(11, '8699593090044', 50, 50, 260.40, '2026-06-17', '2026-04-26'),
(12, '8699593090044', 200, 200, 243.60, '2027-03-15', '2026-05-21'),
-- Pharmaton
(13, '8699809090038', 30, 30, 192.50, '2026-05-28', '2026-03-27'),
(14, '8699809090038', 50, 50, 217.00, '2026-06-17', '2026-04-26'),
(15, '8699809090038', 200, 200, 203.00, '2027-03-15', '2026-05-21'),
-- Beloc Zok
(16, '8699508090409', 30, 30, 52.39, '2026-05-28', '2026-03-27'),
(17, '8699508090409', 50, 50, 59.06, '2026-06-17', '2026-04-26'),
(18, '8699508090409', 200, 200, 55.25, '2027-03-15', '2026-05-21'),
-- Zinnat
(19, '8699522010045', 30, 30, 115.50, '2026-05-28', '2026-03-27'),
(20, '8699522010045', 50, 50, 130.20, '2026-06-17', '2026-04-26'),
(21, '8699522010045', 200, 200, 121.80, '2027-03-15', '2026-05-21'),
-- Lipitor
(22, '8699544010072', 30, 30, 96.53, '2026-05-28', '2026-03-27'),
(23, '8699544010072', 50, 50, 108.81, '2026-06-17', '2026-04-26'),
(24, '8699544010072', 200, 200, 101.79, '2027-03-15', '2026-05-21'),
-- Nexium
(25, '8699566010098', 30, 30, 159.50, '2026-05-28', '2026-03-27'),
(26, '8699566010098', 50, 50, 179.80, '2026-06-17', '2026-04-26'),
(27, '8699566010098', 200, 200, 168.20, '2027-03-15', '2026-05-21'),
-- Ritalin
(28, '8699588010124', 30, 30, 209.00, '2026-05-28', '2026-03-27'),
(29, '8699588010124', 50, 50, 235.60, '2026-06-17', '2026-04-26'),
(30, '8699588010124', 200, 200, 220.40, '2027-03-15', '2026-05-21'),
-- Tamiflu
(31, '8699610010155', 30, 30, 286.00, '2026-05-28', '2026-03-27'),
(32, '8699610010155', 50, 50, 322.40, '2026-06-17', '2026-04-26'),
(33, '8699610010155', 200, 200, 301.60, '2027-03-15', '2026-05-21'),
-- Voltaren
(34, '8699632010186', 30, 30, 35.75, '2026-05-28', '2026-03-27'),
(35, '8699632010186', 50, 50, 40.30, '2026-06-17', '2026-04-26'),
(36, '8699632010186', 200, 200, 37.70, '2027-03-15', '2026-05-21'),
-- Cipro
(37, '8699654010216', 30, 30, 101.75, '2026-05-28', '2026-03-27'),
(38, '8699654010216', 50, 50, 114.70, '2026-06-17', '2026-04-26'),
(39, '8699654010216', 200, 200, 107.30, '2027-03-15', '2026-05-21'),
-- Risperdal
(40, '8699676010247', 30, 30, 247.50, '2026-05-28', '2026-03-27'),
(41, '8699676010247', 50, 50, 279.00, '2026-06-17', '2026-04-26'),
(42, '8699676010247', 200, 200, 261.00, '2027-03-15', '2026-05-21'),
-- Gaviscon
(43, '8699698010278', 30, 30, 43.40, '2026-05-28', '2026-03-27'),
(44, '8699698010278', 50, 50, 48.92, '2026-06-17', '2026-04-26'),
(45, '8699698010278', 200, 200, 45.76, '2027-03-15', '2026-05-21');
