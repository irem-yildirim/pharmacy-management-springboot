-- SQL Migration: Update pres_type to new 5-tier system
-- Run this in MySQL Workbench against your pharmacy database
-- Drops Normal, reassigns drugs with pres_id=1 (Normal) to White, updates risk levels

UPDATE pres_type SET name = 'White',  risk_level = 1 WHERE name IN ('Normal', 'Normal Reçete');
UPDATE pres_type SET name = 'Orange', risk_level = 2 WHERE name IN ('Turuncu Reçete');
UPDATE pres_type SET name = 'Purple', risk_level = 2 WHERE name IN ('Mor Reçete');
UPDATE pres_type SET name = 'Green',  risk_level = 3 WHERE name IN ('Yeşil Reçete');
UPDATE pres_type SET name = 'Red',    risk_level = 4 WHERE name IN ('Kırmızı Reçete');

-- Remove any leftover Normal entry
DELETE FROM pres_type WHERE name = 'Normal';
