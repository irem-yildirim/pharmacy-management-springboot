-- SQL Migration: Translate pres_type names from Turkish to English
-- Run this in MySQL Workbench against your pharmacy database

UPDATE pres_type SET name = 'Normal'  WHERE name = 'Normal Reçete';
UPDATE pres_type SET name = 'White'   WHERE name = 'Beyaz Reçete';
UPDATE pres_type SET name = 'Green'   WHERE name = 'Yeşil Reçete';
UPDATE pres_type SET name = 'Red'     WHERE name = 'Kırmızı Reçete';
