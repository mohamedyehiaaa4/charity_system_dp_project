PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    phone TEXT,
    address TEXT,
    role TEXT NOT NULL,
    donor_type TEXT,
    preferred_channel TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS food_categories (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE,
    is_system INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS storage_types (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE,
    is_system INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS donation_statuses (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE,
    is_system INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS donations (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    donor_id INTEGER NOT NULL REFERENCES users(id),
    type TEXT NOT NULL,
    status_id INTEGER NOT NULL REFERENCES donation_statuses(id),
    created_at TEXT NOT NULL,
    rejection_reason TEXT,
    expiry TEXT,
    quantity REAL,
    unit TEXT,
    category_id INTEGER REFERENCES food_categories(id),
    storage_type_id INTEGER REFERENCES storage_types(id),
    pickup_start TEXT,
    pickup_end TEXT,
    amount TEXT,
    add_ons TEXT
);
