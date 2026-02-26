-- Add expressed milk type support and formula/milk dual-amount tracking
ALTER TABLE feeding_logs ADD COLUMN formula_amount_ml NUMERIC(6,1);
