-- Seed USERS
INSERT INTO users (id, name, phone_number, email, password, address, is_confirmed)
VALUES
    (1, 'admin', '+201068673112', 'esmailmohamedhussein0@gmail.com',
     '$2y$10$87E/aEqSK5Gwr4uwyXf9MecedjCu9.MF/LQO0mv5xCsLXkCMk6td2',  -- bcrypt for "admin123"
     'Qus', true),
    (2, 'user', '+201206806013', 'esmailmohamedhussein25@gmail.com',
     '$2y$10$Su/dDzvBzlLc/5AfCIHKK.dm7kYw.g2zE5cJXWgSChKo3qP7yYZxm',  -- bcrypt for "user123"
     'Qena', true);

-- Seed USER_ROLES
INSERT INTO user_roles (user_id, role)
VALUES
    (1, 'ADMIN'),
    (2, 'USER');
