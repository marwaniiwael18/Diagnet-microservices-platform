-- Migration: Create machine_data table with TimescaleDB hypertable
-- Purpose: Store time-series sensor data from industrial machines
-- Date: 2025-11-11

-- Enable TimescaleDB extension
CREATE EXTENSION IF NOT EXISTS timescaledb;

-- Create the main machine_data table
CREATE TABLE IF NOT EXISTS machine_data (
    id BIGSERIAL,
    machine_id VARCHAR(50) NOT NULL,
    timestamp TIMESTAMPTZ NOT NULL,
    temperature DOUBLE PRECISION,
    vibration DOUBLE PRECISION,
    pressure DOUBLE PRECISION,
    humidity DOUBLE PRECISION,
    power_consumption DOUBLE PRECISION,
    rotation_speed DOUBLE PRECISION,
    status VARCHAR(20),
    location VARCHAR(100),
    metadata JSONB,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id, timestamp)
);

-- Create index on machine_id for faster queries
CREATE INDEX IF NOT EXISTS idx_machine_data_machine_id ON machine_data(machine_id, timestamp DESC);

-- Create index on timestamp for time-range queries
CREATE INDEX IF NOT EXISTS idx_machine_data_timestamp ON machine_data(timestamp DESC);

-- Create index on status for filtering
CREATE INDEX IF NOT EXISTS idx_machine_data_status ON machine_data(status);

-- Create composite index for common query patterns
CREATE INDEX IF NOT EXISTS idx_machine_data_machine_time ON machine_data(machine_id, timestamp DESC, status);

-- Convert to TimescaleDB hypertable (partitioned by time)
-- Chunk interval: 7 days (adjust based on data volume)
SELECT create_hypertable('machine_data', 'timestamp', 
    chunk_time_interval => INTERVAL '7 days',
    if_not_exists => TRUE
);

-- Enable compression for older data (data older than 30 days)
ALTER TABLE machine_data SET (
    timescaledb.compress,
    timescaledb.compress_segmentby = 'machine_id',
    timescaledb.compress_orderby = 'timestamp DESC'
);

-- Add compression policy: compress chunks older than 30 days
SELECT add_compression_policy('machine_data', INTERVAL '30 days', if_not_exists => TRUE);

-- Add retention policy: drop chunks older than 1 year (optional)
-- SELECT add_retention_policy('machine_data', INTERVAL '1 year', if_not_exists => TRUE);

-- Create continuous aggregate for hourly statistics (for faster analytics)
CREATE MATERIALIZED VIEW IF NOT EXISTS machine_data_hourly
WITH (timescaledb.continuous) AS
SELECT 
    machine_id,
    time_bucket('1 hour', timestamp) AS bucket,
    AVG(temperature) as avg_temperature,
    MAX(temperature) as max_temperature,
    MIN(temperature) as min_temperature,
    AVG(vibration) as avg_vibration,
    MAX(vibration) as max_vibration,
    MIN(vibration) as min_vibration,
    AVG(pressure) as avg_pressure,
    AVG(humidity) as avg_humidity,
    AVG(power_consumption) as avg_power_consumption,
    AVG(rotation_speed) as avg_rotation_speed,
    COUNT(*) as data_points
FROM machine_data
GROUP BY machine_id, bucket;

-- Add refresh policy for continuous aggregate (refresh every hour)
SELECT add_continuous_aggregate_policy('machine_data_hourly',
    start_offset => INTERVAL '3 hours',
    end_offset => INTERVAL '1 hour',
    schedule_interval => INTERVAL '1 hour',
    if_not_exists => TRUE
);

-- Create view for daily statistics
CREATE MATERIALIZED VIEW IF NOT EXISTS machine_data_daily
WITH (timescaledb.continuous) AS
SELECT 
    machine_id,
    time_bucket('1 day', timestamp) AS bucket,
    AVG(temperature) as avg_temperature,
    MAX(temperature) as max_temperature,
    MIN(temperature) as min_temperature,
    AVG(vibration) as avg_vibration,
    MAX(vibration) as max_vibration,
    MIN(vibration) as min_vibration,
    AVG(pressure) as avg_pressure,
    AVG(humidity) as avg_humidity,
    AVG(power_consumption) as avg_power_consumption,
    AVG(rotation_speed) as avg_rotation_speed,
    COUNT(*) as data_points
FROM machine_data
GROUP BY machine_id, bucket;

-- Add refresh policy for daily aggregate
SELECT add_continuous_aggregate_policy('machine_data_daily',
    start_offset => INTERVAL '7 days',
    end_offset => INTERVAL '1 day',
    schedule_interval => INTERVAL '1 day',
    if_not_exists => TRUE
);

-- Insert sample data for testing
INSERT INTO machine_data (machine_id, timestamp, temperature, vibration, pressure, humidity, power_consumption, rotation_speed, status, location)
VALUES 
    ('MACHINE-001', NOW() - INTERVAL '1 hour', 75.5, 0.45, 2.3, 45.0, 150.0, 1500.0, 'RUNNING', 'Factory Floor A'),
    ('MACHINE-001', NOW() - INTERVAL '50 minutes', 76.2, 0.47, 2.35, 46.0, 152.0, 1505.0, 'RUNNING', 'Factory Floor A'),
    ('MACHINE-001', NOW() - INTERVAL '40 minutes', 78.0, 0.52, 2.4, 47.0, 155.0, 1510.0, 'RUNNING', 'Factory Floor A'),
    ('MACHINE-001', NOW() - INTERVAL '30 minutes', 92.5, 0.78, 2.6, 48.0, 180.0, 1550.0, 'WARNING', 'Factory Floor A'),
    ('MACHINE-001', NOW() - INTERVAL '20 minutes', 95.0, 0.82, 2.7, 49.0, 190.0, 1580.0, 'WARNING', 'Factory Floor A'),
    ('MACHINE-001', NOW() - INTERVAL '10 minutes', 77.0, 0.48, 2.38, 46.5, 153.0, 1508.0, 'RUNNING', 'Factory Floor A'),
    ('MACHINE-002', NOW() - INTERVAL '1 hour', 72.0, 0.40, 2.2, 43.0, 145.0, 1480.0, 'RUNNING', 'Factory Floor B'),
    ('MACHINE-002', NOW() - INTERVAL '30 minutes', 73.5, 0.42, 2.25, 44.0, 148.0, 1490.0, 'RUNNING', 'Factory Floor B'),
    ('MACHINE-003', NOW() - INTERVAL '1 hour', 105.0, 0.95, 3.0, 55.0, 220.0, 1650.0, 'CRITICAL', 'Factory Floor C'),
    ('MACHINE-003', NOW() - INTERVAL '30 minutes', 108.0, 0.98, 3.1, 56.0, 230.0, 1680.0, 'CRITICAL', 'Factory Floor C');

-- Create function to clean up old test data (optional, for development)
CREATE OR REPLACE FUNCTION cleanup_old_data(retention_days INTEGER)
RETURNS INTEGER AS $$
DECLARE
    deleted_count INTEGER;
BEGIN
    DELETE FROM machine_data 
    WHERE timestamp < NOW() - (retention_days || ' days')::INTERVAL;
    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    RETURN deleted_count;
END;
$$ LANGUAGE plpgsql;

-- Grant permissions (adjust based on your database user)
-- GRANT ALL PRIVILEGES ON TABLE machine_data TO diagnet_user;
-- GRANT ALL PRIVILEGES ON TABLE machine_data_hourly TO diagnet_user;
-- GRANT ALL PRIVILEGES ON TABLE machine_data_daily TO diagnet_user;

COMMENT ON TABLE machine_data IS 'Time-series data from industrial machines with sensor readings';
COMMENT ON COLUMN machine_data.machine_id IS 'Unique identifier for the machine';
COMMENT ON COLUMN machine_data.timestamp IS 'Timestamp when the data was collected (partition key)';
COMMENT ON COLUMN machine_data.temperature IS 'Temperature in Celsius';
COMMENT ON COLUMN machine_data.vibration IS 'Vibration level (0-1 scale)';
COMMENT ON COLUMN machine_data.pressure IS 'Pressure in bar';
COMMENT ON COLUMN machine_data.metadata IS 'Additional JSON metadata for extensibility';
