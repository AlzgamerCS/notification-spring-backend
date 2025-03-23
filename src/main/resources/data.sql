-- Test Users (password is 'password' encoded with BCrypt)
INSERT INTO users (id, name, email, password_hash, role, created_at)
VALUES 
('22222222-2222-2222-2222-222222222222', 'Manager User', 'manager@example.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'MANAGER', CURRENT_TIMESTAMP),
('33333333-3333-3333-3333-333333333333', 'Regular User', 'user@example.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'USER', CURRENT_TIMESTAMP);

-- Admin user (password is 'admin' encoded with BCrypt)
INSERT INTO users (id, name, email, password_hash, role, created_at)
VALUES 
('11111111-1111-1111-1111-111111111111', 'Admin User', 'admin@example.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'ADMIN', CURRENT_TIMESTAMP)
ON CONFLICT (email) DO UPDATE 
SET password_hash = EXCLUDED.password_hash;

-- Test Documents
INSERT INTO documents (id, title, description, category, expiration_date, status, owner_id, created_at, updated_at)
VALUES 
('44444444-4444-4444-4444-444444444444', 'Business License', 'Company business license', 'LICENSE', CURRENT_DATE + 30, 'ACTIVE', '22222222-2222-2222-2222-222222222222', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('55555555-5555-5555-5555-555555555555', 'Safety Certificate', 'Workplace safety certification', 'CERTIFICATION', CURRENT_DATE + 60, 'ACTIVE', '22222222-2222-2222-2222-222222222222', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Document Tags
INSERT INTO document_tags (document_id, tag)
VALUES 
('44444444-4444-4444-4444-444444444444', 'business'),
('44444444-4444-4444-4444-444444444444', 'license'),
('55555555-5555-5555-5555-555555555555', 'safety'),
('55555555-5555-5555-5555-555555555555', 'certification');

-- Document Assignments
INSERT INTO document_assignments (id, document_id, user_id, is_primary)
VALUES 
('66666666-6666-6666-6666-666666666666', '44444444-4444-4444-4444-444444444444', '33333333-3333-3333-3333-333333333333', true),
('77777777-7777-7777-7777-777777777777', '55555555-5555-5555-5555-555555555555', '33333333-3333-3333-3333-333333333333', true);

-- Notification Preferences
INSERT INTO notification_preferences (id, user_id, channel, enabled)
VALUES 
('88888888-8888-8888-8888-888888888888', '33333333-3333-3333-3333-333333333333', 'EMAIL', true),
('99999999-9999-9999-9999-999999999999', '33333333-3333-3333-3333-333333333333', 'IN_APP', true);

-- Notification Lead Days
INSERT INTO notification_lead_days (preference_id, lead_days)
VALUES 
('88888888-8888-8888-8888-888888888888', 30),
('88888888-8888-8888-8888-888888888888', 7),
('99999999-9999-9999-9999-999999999999', 14),
('99999999-9999-9999-9999-999999999999', 3); 