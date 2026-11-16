-- Cab Booking Application Schema (PostgreSQL / Supabase compatible)

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    role VARCHAR(20) NOT NULL, -- ROLE_RIDER, ROLE_DRIVER, ROLE_ADMIN
    avatar_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS drivers (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    license_number VARCHAR(50) UNIQUE NOT NULL,
    vehicle_model VARCHAR(100) NOT NULL,
    vehicle_number VARCHAR(50) UNIQUE NOT NULL,
    vehicle_type VARCHAR(30) NOT NULL, -- ECONOMY, COMFORT, PREMIER, XL
    status VARCHAR(20) DEFAULT 'OFFLINE', -- AVAILABLE, BUSY, OFFLINE
    current_lat DOUBLE PRECISION,
    current_lng DOUBLE PRECISION,
    rating DOUBLE PRECISION DEFAULT 5.0,
    total_rides INT DEFAULT 0,
    total_earnings NUMERIC(10, 2) DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS rides (
    id BIGSERIAL PRIMARY KEY,
    rider_id BIGINT NOT NULL REFERENCES users(id),
    driver_id BIGINT REFERENCES drivers(id),
    pickup_address VARCHAR(255) NOT NULL,
    pickup_lat DOUBLE PRECISION NOT NULL,
    pickup_lng DOUBLE PRECISION NOT NULL,
    drop_address VARCHAR(255) NOT NULL,
    drop_lat DOUBLE PRECISION NOT NULL,
    drop_lng DOUBLE PRECISION NOT NULL,
    vehicle_type VARCHAR(30) NOT NULL,
    distance_km DOUBLE PRECISION NOT NULL,
    duration_minutes INT NOT NULL,
    fare NUMERIC(10, 2) NOT NULL,
    discount NUMERIC(10, 2) DEFAULT 0.00,
    final_fare NUMERIC(10, 2) NOT NULL,
    status VARCHAR(30) NOT NULL, -- REQUESTED, ACCEPTED, ARRIVED, IN_PROGRESS, COMPLETED, CANCELLED
    otp VARCHAR(6),
    payment_status VARCHAR(30) DEFAULT 'UNPAID', -- UNPAID, PAID, REFUNDED
    cancellation_reason VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    started_at TIMESTAMP,
    completed_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS payments (
    id BIGSERIAL PRIMARY KEY,
    ride_id BIGINT UNIQUE NOT NULL REFERENCES rides(id) ON DELETE CASCADE,
    rider_id BIGINT NOT NULL REFERENCES users(id),
    amount NUMERIC(10, 2) NOT NULL,
    currency VARCHAR(10) DEFAULT 'USD',
    stripe_payment_intent_id VARCHAR(100),
    stripe_client_secret VARCHAR(255),
    status VARCHAR(30) NOT NULL, -- PENDING, COMPLETED, FAILED, REFUNDED
    payment_method VARCHAR(50) DEFAULT 'CARD',
    receipt_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS ratings (
    id BIGSERIAL PRIMARY KEY,
    ride_id BIGINT UNIQUE NOT NULL REFERENCES rides(id) ON DELETE CASCADE,
    rider_id BIGINT NOT NULL REFERENCES users(id),
    driver_id BIGINT NOT NULL REFERENCES drivers(id),
    score INT NOT NULL CHECK (score >= 1 AND score <= 5),
    comments TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS promo_codes (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    discount_percent INT NOT NULL,
    max_discount NUMERIC(10, 2) NOT NULL,
    min_fare NUMERIC(10, 2) DEFAULT 0.00,
    valid_until TIMESTAMP,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS support_tickets (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    ride_id BIGINT REFERENCES rides(id),
    subject VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    status VARCHAR(30) DEFAULT 'OPEN', -- OPEN, IN_PROGRESS, RESOLVED, CLOSED
    priority VARCHAR(20) DEFAULT 'MEDIUM', -- LOW, MEDIUM, HIGH, URGENT
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_rides_rider_id ON rides(rider_id);
CREATE INDEX IF NOT EXISTS idx_rides_driver_id ON rides(driver_id);
CREATE INDEX IF NOT EXISTS idx_rides_status ON rides(status);
CREATE INDEX IF NOT EXISTS idx_drivers_status ON drivers(status);
