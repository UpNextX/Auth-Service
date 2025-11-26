-- Insert admin user
WITH admin_insert AS (
INSERT INTO users (name, phone_number, email, password, address, is_confirmed)
VALUES (
    'admin',
    '+201068673112',
    'esmailmohamedhussein0@gmail.com',
    '$2y$10$87E/aEqSK5Gwr4uwyXf9MecedjCu9.MF/LQO0mv5xCsLXkCMk6td2',
    'Qus',
    true
    )
ON CONFLICT (email) DO NOTHING
    RETURNING id
    ),
    admin_id AS (
SELECT id FROM admin_insert
UNION
SELECT id FROM users WHERE email = 'esmailmohamedhussein0@gmail.com'
    )
INSERT INTO user_roles (user_id, role)
SELECT id, 'ADMIN' FROM admin_id
    ON CONFLICT (user_id, role) DO NOTHING;



-- Insert normal user
WITH user_insert AS (
INSERT INTO users (name, phone_number, email, password, address, is_confirmed)
VALUES (
    'user',
    '+201206806013',
    'esmailmohamedhussein25@gmail.com',
    '$2y$10$Su/dDzvBzlLc/5AfCIHKK.dm7kYw.g2zE5cJXWgSChKo3qP7yYZxm',
    'Qena',
    true
    )
ON CONFLICT (email) DO NOTHING
    RETURNING id
    ),
    user_id AS (
SELECT id FROM user_insert
UNION
SELECT id FROM users WHERE email = 'esmailmohamedhussein25@gmail.com'
    )
INSERT INTO user_roles (user_id, role)
SELECT id, 'USER' FROM user_id
    ON CONFLICT (user_id, role) DO NOTHING;
