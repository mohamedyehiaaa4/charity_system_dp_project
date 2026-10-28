INSERT OR IGNORE INTO users (name, email, role, preferred_channel)
VALUES ('Coordinator', 'coordinator@fooddonation.org', 'COORDINATOR', 'IN_APP');

INSERT OR IGNORE INTO donation_statuses (name, is_system) VALUES
    ('Submitted', 1), ('Approved', 1), ('Rejected', 1), ('Picked up', 1), ('Delivered', 1);

INSERT OR IGNORE INTO food_categories (name) VALUES ('Bakery'), ('Dairy'), ('Meals'), ('Produce');

INSERT OR IGNORE INTO storage_types (name) VALUES ('Room temperature'), ('Refrigerated'), ('Frozen');
